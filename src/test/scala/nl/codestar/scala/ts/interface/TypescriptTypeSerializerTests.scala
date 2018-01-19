package nl.codestar.scala.ts.interface

import nl.codestar.scala.ts.interface.TypescriptType.{TSLiteral, TSString}
import org.scalatest.{FlatSpec, Matchers}
import nl.codestar.scala.ts.interface.dsl._

class TypescriptTypeSerializerTests
    extends FlatSpec
    with Matchers
    with DefaultTSTypes {
  import org.scalactic._

  def whiteSpaceNormalised: Uniformity[String] =
    new AbstractStringUniformity {

      /**Returns the string with all consecutive white spaces reduced to a single space, then removes empty lines.*/
      def normalized(s: String): String = s.replaceAll("\\s+", " ")
      override def toString: String = "whiteSpaceNormalised"
    }

  // Scala 2.11.11 (maybe others) give false positive unused warnings if a class is used only as a generic
  def ignoreUnused(o: Object) = ()

  "The Typescript serializer" should "serialize to a simple interface" in {
    case class Person(name: String, age: Int)

    ignoreUnused(Person("", 0))

    implicit val personTsWrites: TSIType[Person] = TSType.fromCaseClass

    val typescript = TypescriptTypeSerializer.emit[Person]

    typescript.trim should equal("""interface IPerson {
        |  name: string
        |  age: number
        |}""".stripMargin)(after being whiteSpaceNormalised)
  }

  it should "be able to generate multiple typescript interfaces for a nested case classes" in {
    case class ComplexCaseClass(nested: NestedCaseClass)
    case class NestedCaseClass(name: String)

    ignoreUnused(ComplexCaseClass(null))
    ignoreUnused(NestedCaseClass(null))

    implicit val nestedCaseClassTSType: TSIType[NestedCaseClass] =
      TSType.fromCaseClass
    implicit val complexCaseClassTSType: TSIType[ComplexCaseClass] =
      TSType.fromCaseClass

    val typescript = TypescriptTypeSerializer.emit[ComplexCaseClass]

    typescript.trim should equal("""interface INestedCaseClass {
        |  name: string
        |}
        |
        |interface IComplexCaseClass {
        |  nested: INestedCaseClass
        |}""".stripMargin)(after being whiteSpaceNormalised)
  }

  it should "be able to handle options in a case class" in {
    case class OptionCaseClass(option: Option[String])

    ignoreUnused(OptionCaseClass(null))

    implicit val optionCaseClassTSType: TSIType[OptionCaseClass] =
      TSType.fromCaseClass

    val typescript = TypescriptTypeSerializer.emit[OptionCaseClass]

    typescript.trim should equal("""interface IOptionCaseClass {
        |  option?: string
        |}""".stripMargin)(after being whiteSpaceNormalised)
  }

  it should "handle recursive types" in {
    case class A(b: B)
    case class B(a: A)

    ignoreUnused(A(null))
    ignoreUnused(B(null))

    implicit val tsA: TSType[A] = "IA"
    implicit val tsB: TSIType[B] = TSType.fromCaseClass
    val tsAGenerated: TSIType[A] = TSType.fromCaseClass

    TypescriptTypeSerializer
      .emit(tsAGenerated)
      .replaceAll("\\s", "") should equal("""
        |interface IB {
        |  a: IA
        |}
        |
        |interface IA {
        |  b: IB
        |}""".stripMargin.replaceAll("\\s", ""))
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
      TSType.fromCaseClass

    val typescript = TypescriptTypeSerializer.emit[PrimitiveTypes]

    typescript.trim should equal("""interface IPrimitiveTypes {
        |  char: number
        |  string: string
        |  byte: number
        |  short: number
        |  int: number
        |  long: number
        |  float: number
        |  double: number
        |  boolean: boolean
        |  stringSeq: string[]
        |}""".stripMargin)(after being whiteSpaceNormalised)
  }

  it should "serialize an indexed interface" in {
    case class Something(
        values: Map[String, String] = Map("a" -> "b")
    )

    ignoreUnused(Something())

    implicit val somethingTSType: TSIType[Something] = TSType.fromCaseClass

    val typescript = TypescriptTypeSerializer.emit[Something]

    typescript.trim should equal("""interface ISomething {
      |  values: { [ key: string ]: string }
      |}""".stripMargin)(after being whiteSpaceNormalised)
  }

  it should "serialize a named indexed interface" in {
    case class Something(
        values: Map[String, String] = Map("a" -> "b")
    )

    ignoreUnused(Something())

    implicit val somethingTSType: TSNamedType[Something] =
      TSType.interfaceIndexed(name = "ISomething",
                              indexName = "as",
                              indexType = TSString,
                              valueType = TSString)

    val typescript = TypescriptTypeSerializer.emit[Something]

    typescript.trim should equal("""interface ISomething {
        |  [ as: string ]: string
        |}""".stripMargin)(after being whiteSpaceNormalised)
  }

  it should "be able to handle string literal types" in {
    abstract class Value(val name: String)

    object Value {
      def entries: Seq[Value] = Seq(
        Value1,
        Value2
      )

      case object Value1 extends Value("value-1")
      case object Value2 extends Value("value-2")
    }

    implicit val valueTSType: TSNamedType[Value] =
      TSNamedType(TSLiteral("value", Value.entries.map(_.name)))

    val typescript = TypescriptTypeSerializer.emit[Value]

    typescript should equal("""type value = "value-1" | "value-2"""")
  }

  it should "be able to handle numeric literal types" in {
    abstract class Value(val name: Int)

    object Value {
      def entries: Seq[Value] = Seq(
        Value1,
        Value2
      )

      case object Value1 extends Value(1)
      case object Value2 extends Value(2)
    }

    implicit val valueTSType: TSNamedType[Value] =
      TSNamedType(TSLiteral("value", Value.entries.map(_.name)))

    val typescript = TypescriptTypeSerializer.emit[Value]

    typescript should equal("""type value = 1 | 2""")
  }
}
