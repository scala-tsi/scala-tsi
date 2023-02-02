package com.scalatsi

import com.scalatsi.TypescriptType.*
import com.scalatsi.TypescriptTypeToExpr.given

import scala.collection.immutable.ListMap
import scala.quoted.*

object Macros {
  inline def generateInterfaceFromCaseClass[T]: TSIType[T]               = ${ generateInterfaceFromCaseClassImpl[T] }
  inline def getImplicitMappingOrGenerateDefault[T]                      = ${ getImplicitMappingOrGenerateDefaultImpl[T] }
  inline def getImplicitNamedMappingOrGenerateDefault[T]                 = ${ getImplicitNamedMappingOrGenerateDefaultImpl[T] }
  inline def getImplicitInterfaceMappingOrGenerateDefault[T]: TSIType[T] = ${ getImplicitInterfaceMappingOrGenerateDefaultImpl[T] }
  inline def generateUnionFromSealedTrait[T]: TSNamedType[T]             = ${ generateUnionFromSealedTraitImpl[T] }

  private def getTSType[T: Type](using q: Quotes): Expr[TSType[T]] = {
    import q.reflect.*
    val existing = Expr.summon[TSType[T]]
    if (existing.isDefined) {
      return existing.get
    }
    if (TypeRepr.of[T].typeArgs.isEmpty) {
      '{ TSType.getOrGenerate[T] }
    } else {
      val allTypeArguments: Iterator[Type[_]] = findNestedTypeParameters(TypeRepr.of[T]).map(_.asType)

      val typeArgImplicits: List[Expr[Unit]] =
        allTypeArguments.distinct
          .filterNot({ case '[t] => Expr.summon[TSType[t]].isDefined })
          .zipWithIndex
          .map { case ('[t], i) =>
            // TODO: The reason that nested generation doesn't work might be that this is a lookup inside
            //  this macro instead of delaying it until the next compilation cycle.
            // See https://github.com/lampepfl/dotty-macro-examples/blob/55c7e8aacb7738cb9020ed25f67bbacd7c2f28d5/macroTypeClassDerivation/src/macro.scala#LL77C13-L77C13
            // for what might work.
            ValDef
              .let(Symbol.spliceOwner, s"targ${i}Val", getTSType[t].asTerm) { targVal =>
                ('{ given TSType[t] = ${ targVal.asExprOf[TSType[t]] } }).asTerm
              }
              .asExprOf[Unit]
          }
          .toList
      Expr.block(typeArgImplicits, '{ TSType.getOrGenerate[T] })
    }
  }

  private def findNestedTypeParameters(using q: Quotes)(typeRepr: q.reflect.TypeRepr, first: Boolean = true): Iterator[q.reflect.TypeRepr] =
    typeRepr.typeArgs.iterator.flatMap(t => findNestedTypeParameters(t, first = false)) ++ (if (first) Iterator() else Iterator(typeRepr))

  def getImplicitMappingOrGenerateDefaultImpl[T: Type](using Quotes): Expr[TSType[T]] =
    Expr.summon[TSType[T]].getOrElse(generateDefaultMapping[T])

  def getImplicitNamedMappingOrGenerateDefaultImpl[T: Type](using Quotes): Expr[TSNamedType[T]] =
    Expr.summon[TSNamedType[T]].getOrElse(generateDefaultMapping[T].asInstanceOf[Expr[TSNamedType[T]]])

  def getImplicitInterfaceMappingOrGenerateDefaultImpl[T: Type](using Quotes): Expr[TSIType[T]] =
    Expr.summon[TSIType[T]].getOrElse(generateInterfaceFromCaseClassImpl[T])

  private def generateDefaultMapping[T: Type](using q: Quotes): Expr[TSType[T]] = {
    import q.reflect.*
    val symbol = TypeRepr.of[T].typeSymbol
    if (!(symbol.isClassDef)) notFound[T]
    else if (symbol.flags is Flags.Case) generateInterfaceFromCaseClassImpl[T]
    else if (symbol.flags is Flags.Sealed) generateUnionFromSealedTraitImpl[T]
    else notFound[T]
  }

  def generateInterfaceFromCaseClassImpl[T: Type](using q: Quotes): Expr[TSIType[T]] = {
    import q.reflect.*
    val typeRepr = TypeRepr.of[T]
    val symbol   = typeRepr.typeSymbol

    if (!(symbol.isClassDef && (symbol.flags is Flags.Case)))
      report.errorAndAbort(s"Expected case class, but found: ${Type.show[T]}")

    val members: Seq[Expr[(String, TypescriptType)]] =
      symbol.caseFields
        .map(member => (member.name, typeRepr.memberType(member).asType))
        .map {
          case (name, '[None.type]) => '{ (${ Expr(name) }, TSNull) }
          case (name, '[Option[t]]) => '{ (${ Expr(name) }, ${ getTSType[t] } | TSUndefined) }
          case (name, '[t])         => '{ (${ Expr(name) }, ${ getTSType[t] }.get) }
        }

    '{ TSIType[T](TSInterface(${ Expr(tsName[T]) }, ListMap(${ Varargs(members) }*))) }
  }

  private def tsName[T: Type](using q: Quotes): String =
    import q.reflect.*
    val symbol = TypeRepr.of[T].typeSymbol
    if (symbol.isClassDef) s"I${symbol.name}" else symbol.name

  def generateUnionFromSealedTraitImpl[T: Type](using q: Quotes): Expr[TSNamedType[T]] = {
    import q.reflect.*
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

  private def notFound[T: Type](using q: Quotes): Expr[TSType[T]] = {
    import q.reflect.*
    val msg = s"Could not find TSType[${Type.show[T]}] in scope and could not generate it"
    report.warning(s"$msg. Did you create and import it?")
    '{ TSType(TSLiteralString(${ Expr(msg) })) }
  }
}
