package nl.codestar.scala.ts.interface

import nl.codestar.scala.ts.interface.TypescriptType._
import org.scalatest.{FlatSpec, Matchers}
import nl.codestar.scala.ts.interface.dsl._

class TypescriptTypeSerializerTests
    extends FlatSpec
    with Matchers
    with DefaultTSTypes {

  import org.scalactic._

  def whiteSpaceNormalised: Uniformity[String] =
    new AbstractStringUniformity {

      /** Returns the string with all consecutive white spaces reduced to a single space, then removes empty lines. */
      def normalized(s: String): String = s.replaceAll("\\s+", " ")

      override def toString: String = "whiteSpaceNormalised"
    }

  // Scala 2.11.11 (maybe others) give false positive unused warnings if a class is used only as a generic
  def ignoreUnused(o: Object): Unit = ()

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

    implicit val tsA: TSType[A] = TSType.external("IA")
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

  it should "handle a type alias with nested types" in {
    val a = TSType.alias("A", TSNumber)
    val b = TSType.alias("B", TSString)
    val aOrB = TSType.alias("AOrB", a | b)

    val typescript = TypescriptTypeSerializer.emit(aOrB).trim

    typescript should include("type A = number")
    typescript should include("type B = string")
    typescript should include("type AOrB = (A | B)")
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

  it should "handle string literal types" in {

    // How we define the Point in our typescript interface
    val expectedPoint = """interface Point {
                          |  type: "Point"
                          |  coords: [number, number]
                          |}""".stripMargin

    // How we define the polygon in our typescript interface
    val expectedPolygon = """interface Polygon {
                            |  type: "Polygon"
                            |  coords: [number, number][]
                            |}""".stripMargin

    sealed trait Geometry
    case class Point(lat: Double, lon: Double) extends Geometry
    case class Polygon(coords: Seq[Point]) extends Geometry

    implicit val pointTSType: TSNamedType[Point] =
      TSType.interface("Point",
                       "type" -> ("Point": TypescriptType),
                       "coords" -> classOf[(Double, Double)])
    implicit val polygonTSType: TSNamedType[Polygon] =
      TSType.interface("Polygon",
                       "type" -> ("Polygon": TypescriptType),
                       "coords" -> classOf[Seq[(Double, Double)]])
    implicit val geometryTSType: TSNamedType[Geometry] =
      TSType.alias("Geometry",
                   implicitly[TSType[Point]] | implicitly[TSType[Polygon]])

    val typescript: String =
      TypescriptTypeSerializer.emits(implicitly[TSNamedType[Geometry]].get).trim

    TypescriptTypeSerializer.emit[Point].trim should equal(expectedPoint)
    typescript should include(expectedPoint)

    TypescriptTypeSerializer.emit[Polygon].trim should equal(expectedPolygon)
    typescript should include(expectedPolygon)

    typescript.trim should include("type Geometry = (Point | Polygon)")
  }

  it should "handle number literals" in {
    val expected = "type FourtyTwo = 42"
    val fourtyTwo = TSType.alias("FourtyTwo", 42)

    val typescript = TypescriptTypeSerializer.emit(fourtyTwo).trim

    typescript should equal(expected)
  }

  it should "handle boolean literals" in {
    val expected = "type MyBool = (true | false)"
    val myBool = TSType.alias("MyBool", (true: TypescriptType) | false)

    val typescript = TypescriptTypeSerializer.emit(myBool).trim

    typescript should equal(expected)
  }

}
