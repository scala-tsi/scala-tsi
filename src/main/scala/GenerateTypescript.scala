import nl.codestar.scala.ts.interface._
import scopt.OptionParser
import TypescriptType._
import nl.codestar.scala.ts.WriteTSToFiles
import nl.codestar.scala.ts.WriteTSToFiles.Config
import nl.codestar.scala.ts.interface.dsl._

object GenerateTypescript extends App with DefaultTSTypes {

  case class IMO(id: String)
  implicit val imoGenerator: TSType[IMO] = tsAlias[IMO, String]

  val union = TSUnion.of(TSString, TSBoolean, TSNull)
  val tuple = TSTuple.of(TSString, union)

  implicit val bazTSType: TSIType[Baz] = tsInterface(
    "boo" -> TSBoolean,
    "baz" -> TSNumber
  )

  implicit val barTSType: TSType[Bar] = TSType.sameAs[Bar, String]

  implicit val fooTSType: TSIType[Foo] = tsInterface(
    "bar" -> classOf[Bar],
    "bool" -> classOf[Boolean],
    "num" -> classOf[Option[Int]],
    "baz" -> classOf[Baz],
    "imo" -> classOf[IMO],
    "union" -> union,
    "tuple" -> tuple
  )

  val A = implicitly[TSNamedType[A]].get

  implicit val baxTSType: TSIType[Bax] = TSIType.fromCaseClass

  val parser = WriteTSToFiles.optionParser

  parser.parse(args, Config()).foreach { config =>
    config.output(emits(classOf[Foo], classOf[Bax]))
  }
}

class Foo(bar: Bar, bool: Boolean, num: Option[Int], baz: Option[Baz])
case class Bar(value: String)
case class Baz(boo: Boolean, bar: Int)
case class Bax(x: Int, y: Int)

/**
  * interface IFoo {
  *   bar: string
  *   bool: boolean
  *   num?: number
  *   baz?: IBaz
  * }
  *
  * interface IBaz {
  *   boo: boolean
  *   bar: number
  * }
  *
  * interface IBax {
  *   x: number
  *   y: number
  * }
  */
