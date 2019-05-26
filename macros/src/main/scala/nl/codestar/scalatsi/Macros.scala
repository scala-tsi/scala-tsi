package nl.codestar.scalatsi

import scala.language.higherKinds
import scala.reflect.macros.blackbox

private[scalatsi] class Macros(val c: blackbox.Context) {
  import c.universe._

  /** Look up an implicit mapping or generate the default */
  private def lookupMapping(T: Type): Tree =
    q"""_root_.nl.codestar.scalatsi.TSType.getOrGenerate[$T]"""

  private def mapToNever[T: c.WeakTypeTag]: Tree =
    q"""{
       import _root_.nl.codestar.scalatsi.TSNamedType
       import _root_.nl.codestar.scalatsi.TypescriptType.{TSAlias, TSNever}
       TSNamedType(TSAlias(${tsName[T]}, TSNever))
     }"""

  /** Change a Type into a "IType" for a class/trait, and "Type" otherwise */
  private def tsName[T: c.WeakTypeTag]: String = {
    val symbol = c.weakTypeOf[T].typeSymbol
    val prefix = for {
      clsSymbol <- if (symbol.isClass) Some(symbol.asClass) else None
      if !clsSymbol.isDerivedValueClass
    } yield "I"

    prefix.getOrElse("") + symbol.name.toString
  }

  def macroUtil = new MacroUtil[c.type](c)

  //private def eval[E](expr: Expr[E]): E = c.eval(c.Expr[E](c.untypecheck(expr.tree.duplicate)))

  def getImplicitMappingOrGenerateDefault[T: c.WeakTypeTag, TSType[_]](implicit tsTypeTag: c.WeakTypeTag[TSType[_]]): Tree = {
    // Get the T => TSType[T] function
    val typeConstructor = c.weakTypeOf[TSType[_]].typeConstructor
    // Construct the TSType[T] type we need to look up
    val lookupType = appliedType(typeConstructor, c.weakTypeOf[T])
    //c.abort(pos = c.enclosingPosition, s"Looking up ${c.weakTypeOf[T]} ${tsTypeTag.tpe} ${tsTypeTag.tpe.typeArgs.head} ${mockType} ${mockType.typeArgs.head}")
    macroUtil.safeLookupOptionalImplicit(lookupType) match {
      case Some(value) => value
      case None        => generateDefaultMapping[T]
    }
  }

  private def generateDefaultMapping[T: c.WeakTypeTag]: Tree = {
    val T      = c.weakTypeOf[T]
    val symbol = T.typeSymbol

    def err(code: Int) = {
      c.abort(
        c.enclosingPosition,
        s"Could not find an implicit TSType[$T] in scope. " +
          s"Could not generate one because it is not a case class or sealed trait. " +
          s"Make sure you created and imported a typescript mapping for the type. (err $code)"
      )
    }

    if (!symbol.isClass) {
      err(1)
    } else {
      val classSymbol = symbol.asClass
      if (classSymbol.isCaseClass)
        generateInterfaceFromCaseClass[T]
      else if (classSymbol.isSealed)
        generateUnionFromSealedTrait[T]
      else err(2)
    }
  }

  private def primaryConstructor(T: Type): MethodSymbol =
    T.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor =>
        if (!m.isPublic)
          c.abort(c.enclosingPosition, s"Only classes with public primary constructor are supported. Found: $T")
        m
    }.get

  private def caseClassFieldsTypes(T: Type): Seq[(String, Type)] = {
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

    params.map { field =>
      (field.name.toTermName.decodedName.toString, field.infoIn(T))
    }
  }

  def generateInterfaceFromCaseClass[T: c.WeakTypeTag]: Tree = {
    val T      = c.weakTypeOf[T]
    val symbol = getClassOrTraitSymbol(T)

    if (!symbol.isCaseClass)
      c.abort(c.enclosingPosition, s"Expected case class, but found: $T")

    val members = caseClassFieldsTypes(T) map {
      case (name, optional) if optional <:< typeOf[Option[_]] =>
        val typeArg = optional.typeArgs.head
        q"($name, ${lookupMapping(typeArg)} | TSUndefined)"
      case (name, tpe) =>
        q"($name, ${lookupMapping(tpe)}.get)"
    }

    q"""{
       import _root_.nl.codestar.scalatsi.TypescriptType.TSUndefined
       import _root_.nl.codestar.scalatsi.{TSNamedType, TSIType}
       import _root_.scala.collection.immutable.ListMap
       TSIType(TSInterface("I" + ${symbol.name.toString}, ListMap(
         ..$members
       )))
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

    if (children.isEmpty) {
      c.warning(c.enclosingPosition, s"Sealed $T has no known subclasses, could not generate union")
      mapToNever[T]
    } else {
      val operands = children map { symbol =>
        q"TypescriptType.nameOrType(${lookupMapping(symbol.asType.toType)}.get)"
      }

      val name = symbol.name.toString

      q"""{
       import _root_.nl.codestar.scalatsi.TypescriptType.{TSAlias, TSUnion}
       import _root_.nl.codestar.scalatsi.TSNamedType
       import _root_.scala.collection.immutable.Vector
       TSNamedType(TSAlias($name, TSUnion(Vector(..$operands))))
      }"""
    }
  }
}
