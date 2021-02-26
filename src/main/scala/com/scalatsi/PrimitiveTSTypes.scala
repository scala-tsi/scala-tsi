package com.scalatsi

import TypescriptType._
import scala.annotation.nowarn

trait PrimitiveTSTypes {
  implicit val booleanTsType: TSType[Boolean]              = TSType(TSBoolean)
  implicit val stringTsType: TSType[String]                = TSType(TSString)
  @nowarn("cat=unused-params") implicit def numberTsType[T: Numeric]: TSType[T] = TSType(TSNumber)
}

object PrimitiveTSTypes extends PrimitiveTSTypes
