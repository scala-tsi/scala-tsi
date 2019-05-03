package nl.codestar.scalatsi

import nl.codestar.scalatsi.TypescriptType._

trait PrimitiveTSTypes {
  implicit val booleanTsType: TSType[Boolean]      = TSType(TSBoolean)
  implicit val stringTsType: TSType[String]        = TSType(TSString)
  implicit def numberTsType[T: Numeric]: TSType[T] = TSType(TSNumber)
}

object PrimitiveTSTypes extends PrimitiveTSTypes
