package com.scalatsi

import TypescriptType.{TSLiteralBoolean, TSLiteralNumber, TSLiteralString}

trait LiteralTSTypes {

  implicit val tsLiteralTrue: TSType[true]   = TSType(TSLiteralBoolean(true))
  implicit val tsLiteralFalse: TSType[false] = TSType(TSLiteralBoolean(false))

  implicit def tsLiteralString[T <: Singleton & String: ValueOf]: TSType[T] = TSType(TSLiteralString(valueOf[T]))

  private def number[T](bd: BigDecimal): TSType[T]                          = TSType(TSLiteralNumber(bd))
  implicit def tsLiteralInt[T <: Singleton & Int: ValueOf]: TSType[T]       = number(BigDecimal(valueOf[T]))
  implicit def tsLiteralLong[T <: Singleton & Long: ValueOf]: TSType[T]     = number(BigDecimal(valueOf[T]))
  implicit def tsLiteralDouble[T <: Singleton & Double: ValueOf]: TSType[T] = number(BigDecimal(valueOf[T]))
  implicit def tsLiteralFloat[T <: Singleton & Float: ValueOf]: TSType[T]   = number(BigDecimal(valueOf[T].toDouble))
}
