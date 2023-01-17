package com.scalatsi

import TypescriptType.*

import scala.annotation.unused

trait PrimitiveTSTypes {
  implicit val booleanTsType: TSType[Boolean]                              = TSType(TSBoolean)
  implicit val stringTsType: TSType[String]                                = TSType(TSString)
  implicit def numberTsType[T](implicit @unused ev: Numeric[T]): TSType[T] = TSType(TSNumber)
}

object PrimitiveTSTypes extends PrimitiveTSTypes
