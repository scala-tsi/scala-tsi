package nl.codestar.scala.ts.interface

import nl.codestar.scala.ts.interface.TypescriptType._

import scala.collection.immutable.ListMap

package object dsl {
  import scala.language.implicitConversions

  implicit class TypescriptTypeDSL(val t: TypescriptType) extends AnyVal {
    def |(tt: TypescriptType): TSUnion = t match {
      case TSUnion(of) => TSUnion(of :+ tt)
      case _           => TSUnion.of(t, tt)
    }

    def array: TSArray = TSArray(t)
  }

  implicit def stringToType[T](s: String): TSType[T] =
    TSNamedType.fromString(s)
  implicit def classToType[T](cls: Class[T])(
      implicit tstype: TSType[T]): TypescriptType =
    tstype.get
  implicit def classToNamedType[T](cls: Class[T])(
      implicit tstype: TSNamedType[T]): TypescriptNamedType =
    tstype.get
  implicit def tupleToTSInterfaceEntry[T](entry: (String, Class[T]))(
      implicit tsType: TSType[T]): (String, TypescriptType) =
    (entry._1, tsType.get)

  implicit class TSInterfaceDSL(val interface: TSInterface) extends AnyVal {
    def +(member: (String, TypescriptType)): TSInterface =
      interface.copy(members = interface.members + member)
    def ++(newMembers: Seq[(String, TypescriptType)]): TSInterface =
      interface.copy(members = interface.members ++ newMembers)
  }

  implicit class TSITypeDSL[T](val tsiType: TSIType[T]) extends AnyVal {
    def +(member: (String, TypescriptType)): TSIType[T] =
      TSIType(tsiType.get + member)
    def ++(newMembers: Seq[(String, TypescriptType)]): TSIType[T] =
      TSIType(tsiType.get ++ newMembers)
  }

  def tsInterface[T](members: (String, TypescriptType)*)(
      implicit ct: Manifest[T]): TSIType[T] =
    tsInterface[T]("I" + ct.runtimeClass.getSimpleName, members: _*)
  def tsInterface[T](name: String,
                     members: (String, TypescriptType)*): TSIType[T] =
    TSIType(TSInterface(name, ListMap(members: _*)))

  def tsInterfaceIndexed[T](name: String,
                            indexName: String = "key",
                            indexType: TypescriptType = TSString,
                            valueType: TypescriptType): TSNamedType[T] =
    TSNamedType(TSInterfaceIndexed(name, indexName, indexType, valueType))

  def tsAlias[T, Alias](implicit tsType: TSType[Alias],
                        ct: Manifest[T]): TSNamedType[T] =
    tsAlias[T, Alias](ct.runtimeClass.getSimpleName)
  def tsAlias[T, Alias](name: String)(
      implicit tsType: TSType[Alias]): TSNamedType[T] =
    tsAlias[T](name, tsType.get)
  def tsAlias[T](name: String, tsType: TypescriptType): TSNamedType[T] =
    TSNamedType(TSAlias(name, tsType))
}
