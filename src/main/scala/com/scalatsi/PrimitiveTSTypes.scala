package com.scalatsi

import TypescriptType._

trait PrimitiveTSTypes {
  implicit val booleanTsType: TSType[Boolean]      = TSType(TSBoolean)
  implicit val stringTsType: TSType[String]        = TSType(TSString)
  implicit def numberTsType[T: Numeric]: TSType[T] = TSType(TSNumber)
}

object PrimitiveTSTypes extends PrimitiveTSTypes
