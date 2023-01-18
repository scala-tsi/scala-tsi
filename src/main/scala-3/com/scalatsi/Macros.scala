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
      val allTypeArguments: Iterator[Type[_]] = findNestedTypeParameters(TypeRepr.of[T]).map(_.asType)

      val typeArgImplicits: List[Expr[Unit]] =
        allTypeArguments
          .distinct
          .filterNot({ case '[t] => Expr.summon[TSType[t]].isDefined })
          .map { case '[t] => '{ given TSType[t]  = TSType.getOrGenerate[t] } }
          .toList
      val x = Expr.block(typeArgImplicits, '{TSType.getOrGenerate[T]})
      println(s"${Type.show[T]} was ${x.show}")
      x
    }
  }

  private def findNestedTypeParameters(typeRepr: TypeRepr, first: Boolean = true): Iterator[TypeRepr] =
    typeRepr.typeArgs.iterator.flatMap(t => findNestedTypeParameters(t, first = false)) ++ (if (first) Iterator() else Iterator(typeRepr))


  def getImplicitMappingOrGenerateDefaultImpl[T: Type]: Expr[TSType[T]] =
    Expr.summon[TSType[T]].getOrElse(generateDefaultMapping[T])

  def getImplicitNamedMappingOrGenerateDefaultImpl[T: Type]: Expr[TSNamedType[T]] =
    Expr.summon[TSNamedType[T]].getOrElse(generateDefaultMapping[T].asInstanceOf[Expr[TSNamedType[T]]])

  def getImplicitInterfaceMappingOrGenerateDefaultImpl[T: Type]: Expr[TSIType[T]] =
    Expr.summon[TSIType[T]].getOrElse(generateInterfaceFromCaseClassImpl[T])

  private def generateDefaultMapping[T: Type]: Expr[TSType[T]] = {
    val symbol = TypeRepr.of[T].typeSymbol
    if (!(symbol.isClassDef)) notFound[T]
    else if (symbol.flags is Flags.Case) generateInterfaceFromCaseClassImpl[T]
    else if (symbol.flags is Flags.Sealed) generateUnionFromSealedTraitImpl[T]
    else notFound[T]
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

    val members: Seq[Expr[(String, TypescriptType)]] =
      symbol.caseFields
        .map(member => (member.name, typeRepr.memberType(member).asType))
        .map {
          case (name, '[None.type]) => '{ (${ Expr(name) }, TSNull) }
          case (name, '[Option[t]]) => '{ (${ Expr(name) }, ${ getTSType[t] } | TSNull) }
          case (name, '[t])         => '{ (${ Expr(name) }, ${ getTSType[t] }.get) }
        }

    '{ TSIType[T](TSInterface(${Expr(tsName[T])}, ListMap(${ Varargs(members) }*))) }
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

  private def notFound[T: Type]: Expr[TSType[T]] = {
    val msg = s"Could not find TSType[${Type.show[T]}] in scope and could not generate it"
    report.warning(s"$msg. Did you create and import it?")
    '{ TSType(TSLiteralString(${Expr(msg)})) }
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
