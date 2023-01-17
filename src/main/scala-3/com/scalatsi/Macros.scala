package com.scalatsi

import com.scalatsi.TypescriptType.*
import com.scalatsi.TypescriptTypeToExpr.given

import scala.collection.immutable.ListMap
import scala.quoted.*
import scala.Symbol

class Macros(using Quotes) {
  import quotes.reflect.*

  private def getTSType[T: Type]: Expr[TSType[T]] = {
    if (TypeRepr.of[T].typeArgs.isEmpty) {
      '{ TSType.getOrGenerate[T] }
    } else {
      def allTypeArgs(typeRepr: TypeRepr, typeReprs: Set[TypeRepr] = Set.empty): Set[TypeRepr] =
        typeRepr.typeArgs
          .flatMap(typeArgRepr => if typeReprs.contains(typeArgRepr) then Set.empty else allTypeArgs(typeArgRepr, typeReprs + typeArgRepr))
          .toSet

      val typeArgImplicits = allTypeArgs(TypeRepr.of[T])
        .tapEach(typeRepr => report.info(s"found type arg implicit ${typeRepr}"))
        .map(_.asType)
        .filterNot({ case '[t] => Expr.summon[TSType[t]].isDefined })
        .map { case '[t] => '{ given TSType[t] = TSType.getOrGenerate[t] } }
        .reduceOption((expr1, expr2) => '{ ${ expr1 }; ${ expr2 } })
        .getOrElse('{})
      '{
        ${ typeArgImplicits }
        TSType.getOrGenerate[T]
      }
    }
  }

  def getImplicitMappingOrGenerateDefaultImpl[T: Type]: Expr[TSType[T]] =
    Expr.summon[TSType[T]].getOrElse(generateDefaultMapping[T])

  def getImplicitNamedMappingOrGenerateDefaultImpl[T: Type]: Expr[TSNamedType[T]] =
    Expr.summon[TSNamedType[T]].getOrElse(generateDefaultMapping[T])

  def getImplicitInterfaceMappingOrGenerateDefaultImpl[T: Type]: Expr[TSIType[T]] =
    Expr.summon[TSIType[T]].getOrElse(generateInterfaceFromCaseClassImpl[T])

  private def generateDefaultMapping[T: Type]: Expr[TSNamedType[T]] = {
    val symbol = TypeRepr.of[T].typeSymbol
    if (!(symbol.isClassDef)) report.errorAndAbort(notFound[T])
    else if (symbol.flags is Flags.Case) generateInterfaceFromCaseClassImpl[T]
    else if (symbol.flags is Flags.Sealed) generateUnionFromSealedTraitImpl[T]
    else report.errorAndAbort(notFound[T])
  }

  def generateInterfaceFromCaseClassImpl[T](using Type[T], Quotes): Expr[TSIType[T]] = {
    val typeRepr = TypeRepr.of[T]
    val symbol   = typeRepr.typeSymbol

    if (!(symbol.isClassDef && (symbol.flags is Flags.Case)))
      report.errorAndAbort(s"Expected case class, but found: ${Type.show[T]}")

    symbol.primaryConstructor.paramSymss match {
      case _ :: Nil =>
      case lists    => report.error(s"Only one parameter list classes are supported. Found: ${Type.show[T]}")
    }

    val members: List[Expr[(String, TypescriptType)]] =
      symbol.caseFields
        .map(member => (member.name, typeRepr.memberType(member).asType))
        .map {
          case (name, '[None.type]) => '{ (${ Expr(name) }, TSNull) }
          case (name, '[Option[t]]) => '{ (${ Expr(name) }, ${ getTSType[t] } | TSNull) }
          case (name, '[t])         => '{ (${ Expr(name) }, ${ getTSType[t] }.get) }
        }

    '{ TSIType[T](TSInterface("I", ListMap(${ Varargs(members) }*))) }
  }

  private def tsName[T: Type]: String =
    val symbol = TypeRepr.of[T].typeSymbol
    if (symbol.isClassDef) s"I${symbol.name}" else symbol.name

  def generateUnionFromSealedTraitImpl[T: Type]: Expr[TSNamedType[T]] = {
    val typeRepr = TypeRepr.of[T]
    val symbol   = typeRepr.typeSymbol

    if (!(symbol.flags is ((Flags.Sealed & (Flags.Abstract | Flags.Trait)))))
      report.errorAndAbort(s"Expected sealed trait or sealed abstract class, but found: ${Type.show[T]}")

    val name = Expr(symbol.name)

    symbol.children.map(_.typeRef.asType) match
      case Seq() =>
        val symbolType =
          if (symbol.flags is Flags.Trait) "trait"
          else if (symbol.flags is Flags.Abstract) "abstract class"
          else "type"
        report.warning(s"Sealed $symbolType ${Type.show[T]} has now known subclasses, could not generate union")
        '{ TSNamedType(TSAlias(${ Expr(tsName[T]) }, TSNever)) }
      case Seq('[t]) => '{ TSNamedType(TSAlias(${ name }, TypescriptType.nameOrType(${ getTSType[t] }.get))) }
      case children =>
        val operands = children.map { tpe =>
          tpe match {
            case '[t] =>
              '{ TypescriptType.nameOrType(${ getTSType[t] }.get, discriminator = Some(${ Expr(TypeRepr.of[t].typeSymbol.name) })) }
          }
        }
        '{ TSNamedType(TSAlias(${ name }, TSUnion(Vector(${ Varargs(operands) }*)))) }
  }

  private def notFound[T: Type]: String = {
    val t = TypeRepr.of[T]
    val isDefault = t =:= TypeRepr.of[String] ||
      t <:< TypeRepr.of[Numeric[_]] ||
      (t <:< TypeRepr.of[Iterable[_]] && !(t <:< TypeRepr.of[Map[_, _]])) ||
      t <:< TypeRepr.of[Either[_, _]] ||
      t <:< TypeRepr.of[Enumeration] ||
      t <:< TypeRepr.of[Enum[_]]
    if (isDefault)
      s"Missing implicit for TSType[${Type.show[T]}]. This should be provided out-of-the-box, please file a bug report at https://github.com/scala-tsi/scala-tsi/issues."
    else
      s"Missing implicit for TSType[${Type.show[T]}] in scope and could not generate one. Did you create and import it?"
  }
}

object Macros {
  private def generateInterfaceFromCaseClassImpl[T](using Quotes, Type[T]): Expr[TSIType[T]] =
    Macros().generateInterfaceFromCaseClassImpl[T]
  inline def generateInterfaceFromCaseClass[T]: TSIType[T] = ${ generateInterfaceFromCaseClassImpl[T] }

  private def getImplicitMappingOrGenerateDefaultImpl[T: Type](using Quotes) = Macros().getImplicitMappingOrGenerateDefaultImpl[T]
  inline def getImplicitMappingOrGenerateDefault[T]                          = ${ getImplicitMappingOrGenerateDefaultImpl[T] }

  private def getImplicitNamedMappingOrGenerateDefaultImpl[T: Type](using Quotes) = Macros().getImplicitNamedMappingOrGenerateDefaultImpl[T]

  inline def getImplicitNamedMappingOrGenerateDefault[T] = ${ getImplicitNamedMappingOrGenerateDefaultImpl[T] }

  private def getImplicitInterfaceMappingOrGenerateDefaultImpl[T: Type](using Quotes): Expr[TSIType[T]] =
    Macros().getImplicitInterfaceMappingOrGenerateDefaultImpl[T]
  inline def getImplicitInterfaceMappingOrGenerateDefault[T]: TSIType[T] = ${ getImplicitInterfaceMappingOrGenerateDefaultImpl[T] }

  private def generateUnionFromSealedTraitImpl[T: Type](using Quotes): Expr[TSNamedType[T]] = Macros().generateUnionFromSealedTraitImpl[T]
  inline def generateUnionFromSealedTrait[T]: TSNamedType[T]                                = ${ generateUnionFromSealedTraitImpl[T] }
}
