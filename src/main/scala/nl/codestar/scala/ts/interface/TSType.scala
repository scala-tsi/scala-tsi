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
trait TSIType[T] extends TSType[T] { self =>
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

trait DefaultTSTypes {
  implicit val BooleanTsType: TSType[Boolean] = TSType(TSBoolean)
  implicit val StringTsType: TSType[String] = TSType(TSString)
  implicit def NumberTsType[T: Numeric]: TSType[T] = TSType(TSNumber)
  implicit def seqTsType[E](implicit e: TSType[E]): TSType[Seq[E]] =
    TSType(TSArray(e.get))
  implicit def optionTsType[E](implicit e: TSType[E]): TSType[Option[E]] =
    TSType(TSUnion.of(e.get, TSUndefined))
}

object DefaultTSTypes extends DefaultTSTypes
