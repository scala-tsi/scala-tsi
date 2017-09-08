package nl.codestar.scala.ts.interface

import org.scalatest.{FlatSpec, Matchers}
import nl.codestar.scala.ts.interface.dsl._

class TypescriptTypeSerializerTests
    extends FlatSpec
    with Matchers
    with DefaultTSTypes {
  // Scala 2.11.11 (maybe others) give false positive unused warnings if a class is used only as a generic
  def ignoreUnused(o: Object) = ()

  "The Typescript serializer" should "serialize to a simple interface" in {
    case class Person(name: String, age: Int)

    ignoreUnused(Person("", 0))

    implicit val personTsWrites: TSIType[Person] = TSIType.fromCaseClass

    val typescript = TypescriptTypeSerializer.emit[Person]

    typescript.trim ===
      """
        |interface IPerson {
        |  name: string
        |  age: number
        |}
      """.stripMargin
  }

  it should "be able to generate multiple typescript interfaces for a nested case classes" in {
    case class ComplexCaseClass(nested: NestedCaseClass)
    case class NestedCaseClass(name: String)

    ignoreUnused(ComplexCaseClass(null))
    ignoreUnused(NestedCaseClass(null))

    implicit val nestedCaseClassTSType: TSIType[NestedCaseClass] =
      TSIType.fromCaseClass
    implicit val complexCaseClassTSType: TSIType[ComplexCaseClass] =
      TSIType.fromCaseClass

    val typescript = TypescriptTypeSerializer.emit[ComplexCaseClass]

    typescript.trim ===
      """
        |interface INestedCaseClass {
        |  name: string
        |}
        |
        |
        |interface IComplexCaseClass {
        |  nested: INestedCaseClass
        |}
      """.stripMargin.trim
  }

  it should "be able to handle options in a case class" in {
    case class OptionCaseClass(option: Option[String])

    ignoreUnused(OptionCaseClass(null))

    implicit val optionCaseClassTSType: TSIType[OptionCaseClass] =
      TSIType.fromCaseClass

    val typescript = TypescriptTypeSerializer.emit[OptionCaseClass]

    typescript.trim ===
      """interface IOptionCaseClass {
        |  option?: string
        |}
      """.stripMargin
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

  it should "be able to handle all primitive types" in {
    case class PrimitiveTypes(
        char: Char,
        string: String,
        byte: Byte,
        short: Short,
        int: Int,
        long: Long,
        float: Float,
        double: Double,
        boolean: Boolean,
        stringSeq: Seq[String]
    )

    ignoreUnused(PrimitiveTypes(0, "", 1, 1, 1, 1, 1, 1, true, Seq.empty))

    implicit val primitiveTypesTSType: TSIType[PrimitiveTypes] =
      TSIType.fromCaseClass

    val typescript = TypescriptTypeSerializer.emit[PrimitiveTypes]

    typescript.trim ===
      """interface IPrimitiveTypes {
        |  char: number
        |  string: string
        |  byte: number
        |  short: number
        |  int: number
        |  long: number
        |  double: number
        |  boolean: boolean
        |  stringSeq: string[]
        |}
      """.stripMargin
  }
}
