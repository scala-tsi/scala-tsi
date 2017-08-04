package nl.codestar.scala.ts.interface

import scala.collection.immutable.ListMap
import scala.reflect.macros.blackbox

private class Macros(val c: blackbox.Context) {
  import c.universe._

  private def primaryConstructor(T: Type): MethodSymbol =
    T.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor =>
        if (!m.isPublic)
          c.error(
            c.enclosingPosition,
            s"Only classes with public primary constructor are supported. Found: $T")
        m
    }.get

  private def caseClassFieldsTypes(T: Type): ListMap[String, Type] = {
    val paramLists = primaryConstructor(T).paramLists
    val params = paramLists.head

    if (paramLists.size > 1)
      c.error(c.enclosingPosition,
              s"Only one parameter list classes are supported. Found: $T")

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

//  def generateInterface[T: c.WeakTypeTag]: Tree = {
//    val T = c.weakTypeOf[T]
//
//    if(!isCaseClass(T))
//      c.error(c.enclosingPosition, s"Expected case class, but found: $T")
//
//    val name = T.typeSymbol.asClass.name
//  }

  def generateInterface[T: c.WeakTypeTag]: Tree = {
    val T = c.weakTypeOf[T]

    if (!isCaseClass(T))
      c.error(c.enclosingPosition, s"Expected case class, but found: $T")

    val members = caseClassFieldsTypes(T).map {
      case (name, tpe) =>
        q"TSInterface.Member($name, implicitly[TSType[$tpe]].get, true)"
    }

    val name = T.typeSymbol.asClass.name

    // TODO: Not get the name through reflection
    q"""{
       import nl.codestar.scala.ts.interface.TypescriptType._
       TSIType(TSInterface(classOf[$name].getSimpleName, Seq(..$members)))
      }"""
  }

  protected def isCaseClass(tpe: Type) =
    tpe.typeSymbol.isClass && tpe.typeSymbol.asClass.isCaseClass
}
