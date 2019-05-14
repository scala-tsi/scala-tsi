package nl.codestar.scalatsi

import scala.collection.immutable.ListMap
import scala.reflect.macros.blackbox

private class Macros(val c: blackbox.Context) {
  import c.universe._

  def generateDefaultMapping[T: c.WeakTypeTag]: Tree = {
    val T      = c.weakTypeOf[T]
    val symbol = getClassSymbol(T)

  }

  private def primaryConstructor(T: Type): MethodSymbol =
    T.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor =>
        if (!m.isPublic)
          c.abort(c.enclosingPosition, s"Only classes with public primary constructor are supported. Found: $T")
        m
    }.get

  private def caseClassFieldsTypes(T: Type): ListMap[String, Type] = {
    val paramLists = primaryConstructor(T).paramLists
    val params     = paramLists.head

    if (paramLists.size > 1)
      c.abort(c.enclosingPosition, s"Only one parameter list classes are supported. Found: $T")

    params.foreach { p =>
      if (!p.isPublic)
        c.abort(
          c.enclosingPosition,
          s"Only classes with all public constructor arguments are supported. Found: $T"
        )
    }

    ListMap(params.map { field =>
      (field.name.toTermName.decodedName.toString, field.infoIn(T))
    }: _*)
  }

  def generateInterfaceFromCaseClass[T: c.WeakTypeTag]: Tree = {
    val T      = c.weakTypeOf[T]
    val symbol = getClassOrTraitSymbol(T)

    if (!symbol.isCaseClass)
      c.abort(c.enclosingPosition, s"Expected case class, but found: $T")

    val members = caseClassFieldsTypes(T) map {
      case (name, optional) if optional <:< typeOf[Option[_]] =>
        val typeArg = optional.typeArgs.head
        q"($name, implicitly[TSType[$typeArg]] | TSUndefined)"
      case (name, tpe) =>
        q"($name, implicitly[TSType[$tpe]].get)"
    }

    q"""{
       import nl.codestar.scalatsi.TypescriptType.TSUndefined
       import nl.codestar.scalatsi.{TSNamedType, TSIType}
       import scala.collection.immutable.ListMap
       TSIType(TSInterface("I" + ${symbol.name.toString}, ListMap(..$members)))
      }"""
  }

  private def getClassOrTraitSymbol(T: Type): ClassSymbol = {
    val symbol = T.typeSymbol
    if (!symbol.isClass)
      c.abort(c.enclosingPosition, s"Expected class or trait, but found $T")
    symbol.asClass
  }

  def generateUnionFromSealedTrait[T: c.WeakTypeTag]: Tree = {
    val T      = c.weakTypeOf[T]
    val symbol = getClassOrTraitSymbol(T)

    if (!symbol.isSealed)
      c.error(c.enclosingPosition, s"Expected sealed trait or class, but found: $T")

    val children = symbol.knownDirectSubclasses.toSeq

    val operands = children map { symbol =>
      q"TypescriptType.nameOrType(implicitly[TSType[${symbol.asType}]].get)"
    }

    q"""{
       import nl.codestar.scalatsi.TypescriptType.{TSAlias, TSUnion}
       import nl.codestar.scalatsi.TypescriptType
       import scala.collection.immutable.Vector
       TSNamedType(TSAlias(${symbol.name.toString}, TSUnion(Vector(..$operands))))
      }"""
  }
}
