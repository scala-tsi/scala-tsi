package nl.codestar.scalatsi

import nl.codestar.scalatsi.TypescriptType.{TSLiteralBoolean, TSLiteralNumber, TSLiteralString}

trait LiteralTSTypes {

  implicit val tsLiteralTrue: TSType[true] = TSType(TSLiteralBoolean(true))
  implicit val tsLiteralFalse: TSType[false] = TSType(TSLiteralBoolean(false))

  implicit def tsLiteralString[T <: Singleton with String: ValueOf]: TSType[T] = TSType(TSLiteralString(valueOf[T]))

  private def number[T](bd: BigDecimal): TSType[T] = TSType(TSLiteralNumber(bd))
  implicit def tsLiteralInt[T <: Singleton with Int : ValueOf]: TSType[T] = number(valueOf[T]: Int)
  implicit def tsLiteralLong[T <: Singleton with Long : ValueOf]: TSType[T] = number(valueOf[T]: Long)
  implicit def tsLiteralDouble[T <: Singleton with Double : ValueOf]: TSType[T] = number(valueOf[T]: Double)
  implicit def tsLiteralFloat[T <: Singleton with Float : ValueOf]: TSType[T] = number(valueOf[T]: Float)
}