package nl.codestar.scala.ts.interface

import scala.annotation.implicitNotFound
import TypescriptType._

/* TODO: Move this somewhere to the docs
 * To define an implicit TSType[T]:
1. If the type maps directly to another type Other, use
    implicit val tsT: TSType[T] = TSType.Of[Other] //or
    implicit val tsT: TSType[T] = tsAlias[T, Other]
2. If T is a case class, use
    implicit val tsT: TSIType[T] = TSIType.fromCaseClass
3. Or use the DSL to build your own interface:
    import nl.codestar.scala.ts.interface.dsl._
    implicit val tsT: TSIType[T] = tsInterface(
      "foo" -> classOf[String],
      "bar" -> classOf[Option[Int]]
    )
 */

@implicitNotFound(
  "Could not find a Typescript type mapping for type ${T}. Make sure an implicit TSType[${T}] or TSIType[${T}] is in scope and was defined before this point.")
trait TSType[T] { self =>
  def get: TypescriptType
  override def equals(obj: scala.Any): Boolean = obj match {
    case o: TSType[_] => get == o.get
    case _ => false
  }
  override def hashCode(): Int = get.hashCode()
  override def toString: String = s"TSType($get)"
}

@implicitNotFound(
  "Could not find a TSNamedType[${T}] in scope. If you have defined a typescript mapping, we can only use typescript types with a name at this location.")
trait TSNamedType[T] extends TSType[T] { self =>
  def get: TypescriptNamedType
  override def toString: String = s"TSNamedType($get)"
}

object TSType {
  private class TSTypeImpl[T](override val get: TypescriptType)
      extends TSType[T]
  def apply[T](tt: TypescriptType): TSType[T] = new TSTypeImpl(tt)

  def sameAs[Target, Source](
      implicit sourceType: TSType[Source]): TSType[Target] =
    TSType(sourceType.get)

  def fromCaseClass[T]: TSIType[T] = macro Macros.generateInterface[T]
}

object TSNamedType {
  private class TSNamedTypeImpl[T](override val get: TypescriptNamedType)
      extends TSNamedType[T]
  def apply[T](tt: TypescriptNamedType): TSNamedType[T] =
    new TSNamedTypeImpl(tt)

  def fromString[T](s: String): TSNamedType[T] =
    TypescriptType.fromString(s) match {
      case t: TSExternalName => TSNamedType(t)
      case t =>
        throw new IllegalArgumentException(
          s"String $s is a predefined type $t")
    }
}

@implicitNotFound(
  "Could not find a TSIType[${T}] in scope. If you have defined a typescript mapping, we can only use typescript interface types at this location.")
trait TSIType[T] extends TSNamedType[T] { self =>
  override def get: TSInterface
  override def toString: String = s"TSIType($get)"
}

object TSIType {
  private class TSITypeImpl[T](override val get: TSInterface)
      extends TSIType[T]
  def apply[T](tt: TSInterface): TSIType[T] = new TSITypeImpl(tt)

  // TODO: Put here or on TSType?
  def fromCaseClass[T]: TSIType[T] = macro Macros.generateInterface[T]
}
