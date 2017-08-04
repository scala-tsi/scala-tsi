package nl.codestar.scala.ts.interface

import nl.codestar.scala.ts.interface.TypescriptType._

package object dsl {
  import scala.language.implicitConversions

  implicit def tuple2InterfaceMemberTs(
      member: (String, TypescriptType)): TSInterface.Member =
    TSInterface.Member.apply(member._1, member._2)
  implicit def tuple2InterfaceMember[T: TSType](
      member: (String, Class[T])
  )(implicit tsType: TSType[T]): TSInterface.Member =
    TSInterface.Member.apply(member._1, tsType.get)

  implicit class TypescriptTypeDSL(val t: TypescriptType) extends AnyVal {
    def |(tt: TypescriptType): TSUnion = t match {
      case TSUnion(of) => TSUnion(of :+ tt)
      case _ => TSUnion.of(t, tt)
    }

    def array: TSArray = TSArray(t)
  }

  implicit class TSInterfaceDSL(val interface: TSInterface) extends AnyVal {
    def +(member: TSInterface.Member): TSInterface =
      interface.copy(members = interface.members :+ member)
    def ++(newMembers: Seq[TSInterface.Member]): TSInterface =
      interface.copy(members = interface.members ++ newMembers)
  }

  implicit class TSITypeDSL[T](val tsiType: TSIType[T]) extends AnyVal {
    def +(member: TSInterface.Member): TSIType[T] =
      TSIType(tsiType.get + member)
    def ++(newMembers: Seq[TSInterface.Member]): TSIType[T] =
      TSIType(tsiType.get ++ newMembers)
  }

  def tsInterface[T](members: TSInterface.Member*)(
      implicit ct: Manifest[T]): TSIType[T] =
    tsInterface[T]("I" + ct.runtimeClass.getSimpleName, members: _*)
  def tsInterface[T](name: String, members: TSInterface.Member*): TSIType[T] =
    TSIType(TSInterface(name, members))
  def tsAlias[T, Alias](implicit tsType: TSType[Alias],
                        ct: Manifest[T]): TSType[T] =
    tsAlias[T, Alias](ct.runtimeClass.getSimpleName)
  def tsAlias[T, Alias](name: String)(
      implicit tsType: TSType[Alias]): TSType[T] =
    TSType(TSAlias(name, tsType.get))
}
