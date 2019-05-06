package nl.codestar.scalatsi

import scala.collection.immutable.ListMap
import scala.reflect.macros.blackbox

private class Macros(val c: blackbox.Context) {
  import c.universe._

  private def primaryConstructor(T: Type): MethodSymbol =
    T.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor =>
        if (!m.isPublic)
          c.error(c.enclosingPosition, s"Only classes with public primary constructor are supported. Found: $T")
        m
    }.get

  private def caseClassFieldsTypes(T: Type): ListMap[String, Type] = {
    val paramLists = primaryConstructor(T).paramLists
    val params     = paramLists.head

    if (paramLists.size > 1)
      c.error(c.enclosingPosition, s"Only one parameter list classes are supported. Found: $T")

    params.foreach { p =>
      if (!p.isPublic)
        c.error(
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
    val symbol = getClassSymbol(T)

    if (!symbol.isCaseClass)
      c.error(c.enclosingPosition, s"Expected case class, but found: $T")

    val members = caseClassFieldsTypes(T) map {
      case (name, optional) if optional <:< typeOf[Option[_]] =>
        val typeArg = optional.typeArgs.head
        q"($name, TSUnion.of(implicitly[TSType[$typeArg]].get, TSUndefined))"
      case (name, tpe) =>
        q"($name, implicitly[TSType[$tpe]].get)"
    }

    q"""{
       import nl.codestar.scalatsi.TypescriptType._
       import nl.codestar.scalatsi.{ TSNamedType, TSIType }
       import scala.collection.immutable.ListMap
       TSIType(TSInterface("I" + ${symbol.name.toString}, ListMap(..$members)))
      }"""
  }

  private def getClassSymbol(T: Type): ClassSymbol = {
    val symbol = T.typeSymbol
    if (!symbol.isClass)
      c.error(c.enclosingPosition, s"Expected class, but found $T")
    symbol.asClass
  }

  protected def isCaseClass(tpe: Type): Boolean =
    tpe.typeSymbol.isClass && tpe.typeSymbol.asClass.isCaseClass

  def generateTypeFromCaseClass[T: c.WeakTypeTag]: Tree = ???
}
