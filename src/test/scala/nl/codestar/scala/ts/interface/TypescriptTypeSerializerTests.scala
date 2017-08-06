package nl.codestar.scala.ts.interface

import org.scalatest.{FlatSpec, Matchers}
import nl.codestar.scala.ts.interface._
import nl.codestar.scala.ts.interface.dsl._

class TypescriptTypeSerializerTests
    extends FlatSpec
    with Matchers
    with DefaultTSTypes {
  // Scala 2.11.11 (maybe others) give false positive unused warnings if a class is used only as a generic
  def ignoreUnused(o: Object) = ()

  "The Typescript serializer" should "serialize to a simple interface" in {
    //ok
  }

  it should "handle recursive types" in {
    case class A(b: B)
    case class B(a: A)

    ignoreUnused(A(null))
    ignoreUnused(B(null))

    implicit val tsA: TSType[A] = "A"
    implicit val tsB: TSIType[B] = TSIType.fromCaseClass
    val tsAGenerated: TSIType[A] = TSIType.fromCaseClass

    TypescriptTypeSerializer.emit(tsAGenerated) ===
      """interface IB {
        |  a: IA
        |}
        |
        |interface IA {
        |  b: IB
        |}
      """.stripMargin
  }
}
