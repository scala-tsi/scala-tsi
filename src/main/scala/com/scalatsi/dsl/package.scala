package com.scalatsi

import TypescriptType.*

import scala.annotation.unused

package object dsl {
  import scala.language.implicitConversions

  // Implicit conversions to allow a more natural DSL
  // disable unused because the cls parameter if for the conversion, but isn't actually used
  implicit def classToType[T](@unused cls: Class[T])(implicit tsType: TSType[T]): TypescriptType =
    tsType.get
  implicit def classToNamedType[T](@unused cls: Class[T])(implicit tsType: TSNamedType[T]): TypescriptNamedType =
    tsType.get
  implicit def tupleToTSInterfaceEntry[T](entry: (String, Class[T]))(implicit tsType: TSType[T]): (String, TypescriptType) =
    (entry._1, tsType.get)

  // Literal types
  implicit def stringToLiteral(s: String): TSLiteralString           = TSLiteralString(s)
  implicit def booleanToLiteral(bool: Boolean): TSLiteralBoolean     = TSLiteralBoolean(bool)
  implicit def intToLiteral(i: Int): TSLiteralNumber                 = TSLiteralNumber(BigDecimal(i))
  implicit def longToLiteral(l: Long): TSLiteralNumber               = TSLiteralNumber(BigDecimal(l))
  implicit def doubleToLiteral(d: Double): TSLiteralNumber           = TSLiteralNumber(BigDecimal(d))
  implicit def bigDecimalToLiteral(big: BigDecimal): TSLiteralNumber = TSLiteralNumber(big)

  // Implicit conversion from typescript types to the TSType typeclasss
  implicit def typescriptTypeToTSType[T <: TypescriptType](tpe: T): TSType[T]                = TSType(tpe)
  implicit def typescriptTypeToTSNamedType[T <: TypescriptNamedType](tpe: T): TSNamedType[T] = TSNamedType(tpe)
  implicit def typescriptTypeToTSIType[T <: TSInterface](tpe: T): TSIType[T]                 = TSIType(tpe)

  implicit class TSInterfaceDSL(val interface: TSInterface) extends AnyVal {
    def +(member: (String, TypescriptType)): TSInterface                = interface.copy(members = interface.members + member)
    def ++(newMembers: Iterable[(String, TypescriptType)]): TSInterface = interface.copy(members = interface.members ++ newMembers)
    def -(member: String): TSInterface                                  = interface.copy(members = interface.members - member)
    def --(members: Iterable[String]): TSInterface                      = interface.copy(members = interface.members -- members)
  }

  implicit class TSITypeDSL[T](val tsiType: TSIType[T]) extends AnyVal {
    def +(member: (String, TypescriptType)): TSIType[T]           = TSIType(tsiType.get + member)
    def ++(newMembers: Seq[(String, TypescriptType)]): TSIType[T] = TSIType(tsiType.get ++ newMembers)
    def -(member: String): TSIType[T]                             = TSIType(tsiType.get - member)
    def --(members: Iterable[String]): TSIType[T]                 = TSIType(tsiType.get -- members)
  }
}
