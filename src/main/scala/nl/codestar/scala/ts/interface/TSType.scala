package nl.codestar.scala.ts.interface

import scala.annotation.implicitNotFound
import TypescriptType._

@implicitNotFound(
  """Could not find a Typescript type mapping for type ${T}.
Make sure an implicit TSType[${T}] or TSIType[${T}] is in scope.
Make sure the initialization ordering is correct.

To define an implicit TSType[T]:
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
""")
trait TSType[T] { self =>
  def get: TypescriptType
}
@implicitNotFound(
  """Could not find a named Typescript type mapping for type ${T}
Make sure an implicit TSNamedType[${T}] or TSIType[${T}] is in scope.
If you have defined a typescript mapping, we can only use typescript types with a name at this location.
""")
trait TSNamedType[T] extends TSType[T] { self =>
  def get: TypescriptNamedType
}
@implicitNotFound(
  """Could not find an interface Typescript type mapping for type ${T}
Make sure an implicit TSIType[${T}] is in scope.
If you have defined a typescript mapping, we can only use typescript interface types at this location.
""")
trait TSIType[T] extends TSNamedType[T] { self =>
  override def get: TSInterface
}

object TSType {
  def apply[T](tt: TypescriptType): TSType[T] = new TSType[T] { val get = tt }

  def of[T](implicit tsType: TSType[T]): TypescriptType = tsType.get
}

object TSIType {
  def apply[T](tt: TSInterface): TSIType[T] = new TSIType[T] { val get = tt }

  def fromCaseClass[T]: TSIType[T] = macro Macros.generateInterface[T]
}
