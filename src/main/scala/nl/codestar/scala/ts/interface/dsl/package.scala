package nl.codestar.scala.ts.interface

import nl.codestar.scala.ts.interface.TypescriptType._

import scala.collection.GenTraversableOnce

package object dsl {
  import scala.language.implicitConversions

  implicit class TypescriptTypeDSL(val t: TypescriptType) extends AnyVal {
    def |(tt: TypescriptType): TSUnion = t match {
      case TSUnion(of) => TSUnion(of :+ tt)
      case _           => TSUnion.of(t, tt)
    }

    def array: TSArray = TSArray(t)
  }

  // Implicit conversions to allow a more natural DSL
  implicit def stringToType[T](s: String): TSType[T] = TSType.external(s)
  implicit def classToType[T](cls: Class[T])(
      implicit tsType: TSType[T]): TypescriptType = tsType.get
  implicit def classToNamedType[T](cls: Class[T])(
      implicit tsType: TSNamedType[T]): TypescriptNamedType = tsType.get
  implicit def tupleToTSInterfaceEntry[T](entry: (String, Class[T]))(
      implicit tsType: TSType[T]): (String, TypescriptType) =
    (entry._1, tsType.get)

  // Implicit conversion from typescript types to the TSType typeclasss
  implicit def typescriptTypeToTSType[T <: TypescriptType](tpe: T): TSType[T] =
    TSType(tpe)
  implicit def typescriptTypeToTSNamedType[T <: TypescriptNamedType](
      tpe: T): TSNamedType[T] = TSNamedType(tpe)
  implicit def typescriptTypeToTSIType[T <: TSInterface](tpe: T): TSIType[T] =
    TSIType(tpe)

  implicit class TSInterfaceDSL(val interface: TSInterface) extends AnyVal {
    def +(member: (String, TypescriptType)): TSInterface =
      interface.copy(members = interface.members + member)
    def ++(newMembers: GenTraversableOnce[(String, TypescriptType)]): TSInterface =
      interface.copy(members = interface.members ++ newMembers)
    def -(member: String): TSInterface =
      interface.copy(members = interface.members - member)
    def --(members: GenTraversableOnce[String]): TSInterface =
      interface.copy(members = interface.members -- members)
  }

  implicit class TSITypeDSL[T](val tsiType: TSIType[T]) extends AnyVal {
    def +(member: (String, TypescriptType)): TSIType[T] =
      TSIType(tsiType.get + member)
    def ++(newMembers: Seq[(String, TypescriptType)]): TSIType[T] =
      TSIType(tsiType.get ++ newMembers)
    def -(member: String): TSIType[T] = TSIType(tsiType.get - member)
    def --(members: GenTraversableOnce[String]): TSIType[T] = TSIType(tsiType.get -- members)
  }
}
