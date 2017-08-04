package nl.codestar.scala.ts.interface

import scala.annotation.implicitNotFound
import TypescriptType._

@implicitNotFound(
  "Could not find a Typescript type mapping for type ${T}. Try to define an implicit TSType[${T}] or TSIType[${T}]"
)
trait TSType[T] { self =>
  def get: TypescriptType
}
trait TSIType[T] extends TSType[T] { self =>
  override def get: TSInterface
}

object TSType {
  def apply[T](tt: TypescriptType): TSType[T] = new TSType[T] { val get = tt }

  def get[T](o: T)(implicit tsType: TSType[T]): TypescriptType = tsType.get
}

object TSIType {
  def apply[T](tt: TSInterface): TSIType[T] = new TSIType[T] { val get = tt }
  def apply[T](name: String,
               members: Seq[TSInterface.Member] = Seq()): TSIType[T] =
    TSIType(TSInterface(name, members))
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
