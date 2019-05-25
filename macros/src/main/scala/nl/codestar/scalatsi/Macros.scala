package nl.codestar.scalatsi

import scala.collection.immutable.ListMap
import scala.reflect.macros.blackbox

private[scalatsi] class Macros(val c: blackbox.Context) {
  import c.universe._

  /** Look up an implicit mapping or generate the default */
  private def lookupMapping(T: TypeSymbol): Tree =
    q"""nl.codestar.scalatsi.TSType.getMappingOrGenerateDefault[$T]"""

  private def mapToNever[T: c.WeakTypeTag]: Tree =
    q"""{
       import nl.codestar.scalatsi.TSNamedType
       import nl.codestar.scalatsi.TypescriptType.{TSAlias, TSNever}
       TSNamedType(TSAlias(${tsName[T]}, TSNever))
     }"""

  /** Change a Type into a "IType" for a class/trait, and "Type" otherwise */
  private def tsName[T: c.WeakTypeTag]: String = {
    val symbol = c.weakTypeOf[T].typeSymbol
    (if (symbol.isClass) "I" else "") + symbol.name.toString
  }

  private def eval[E](expr: Expr[E]): E = c.eval(c.Expr[E](c.untypecheck(expr.tree.duplicate)))

  def getMappingOrGenerateDefault[T: c.WeakTypeTag, TSType](mapping: c.Expr[OptionalImplicit[TSType]]): Tree = {
    /*eval(mapping).value*/ None match {
      //case Some(_) => mapToNever[T] //reify(mapping.value.value.get).tree
      case None    => mapToNever[T] //generateDefaultMapping[T]
    }
  }

  private def generateDefaultMapping[T: c.WeakTypeTag]: Tree = {
    val T      = c.weakTypeOf[T]
    val symbol = T.typeSymbol

    def err() = {
      c.abort(
        c.enclosingPosition,
        s"Could not find an implicit TSType[$T] in scope. " +
          s"Could not generate one because it is not a case class or sealed trait. " +
          s"Make sure you created and imported a typescript mapping for the type."
      )
    }

    if (!symbol.isClass) {
      err()
    } else {
      val classSymbol = symbol.asClass
      if (classSymbol.isCaseClass)
        generateInterfaceFromCaseClass[T]
      else if (classSymbol.isSealed)
        generateUnionFromSealedTrait[T]
      else err()
    }
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
        q"($name, ${lookupMapping(typeArg.typeSymbol.asType)} | TSUndefined)"
      case (name, tpe) =>
        q"($name, ${lookupMapping(tpe.typeSymbol.asType)}.get)"
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
      c.abort(c.enclosingPosition, s"Expected sealed trait or class, but found: $T")

    val children = symbol.knownDirectSubclasses

    if(children.isEmpty) {
      c.warning(c.enclosingPosition, s"Sealed $T has no known subclasses, could not generate union")
      mapToNever[T]
    } else {
      val operands = children map { symbol =>
        q"TypescriptType.nameOrType(${lookupMapping(symbol.asType)}.get)"
      }

      val name = symbol.name.toString

      q"""{
       import nl.codestar.scalatsi.TypescriptType.{TSAlias, TSUnion}
       import nl.codestar.scalatsi.TSNamedType
       import scala.collection.immutable.Vector
       TSNamedType(TSAlias($name, TSUnion(Vector(..$operands))))
      }"""
    }
  }
}
