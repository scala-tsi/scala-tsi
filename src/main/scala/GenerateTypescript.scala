import nl.codestar.scala.ts.interface._
import scopt.OptionParser

import TypescriptType._
import nl.codestar.scala.ts.interface.dsl._

object GenerateTypescript extends App with DefaultTSTypes {

  case class IMO(id: String)
  implicit val imoGenerator: TSType[IMO] = tsAlias[IMO, String]

  val union = TSUnion.of(TSString, TSBoolean, TSNull)
  val tuple = TSTuple.of(TSString, union)

  implicit val bazGenerator: TSIType[Baz] = tsInterface(
    "boo" -> TSBoolean,
    "baz" -> TSNumber
  )

  implicit val fooGenerator: TSIType[Foo] = tsInterface(
    "bar" -> classOf[String],
    "bool" -> classOf[Boolean],
    "num" -> classOf[Option[Int]],
    "baz" -> classOf[Baz],
    "imo" -> classOf[IMO],
    "union" -> union,
    "tuple" -> tuple
  )

  val parser = new OptionParser[Config]("Generate typescript") {}

  import TypescriptTypeSerializer._

  parser.parse(args, Config()).foreach { config =>
    println(emit[Foo])
  }

  case class Config()
}

class Foo(bar: Bar, bool: Boolean, num: Option[Int], baz: Option[Baz])
case class Bar(value: String)
case class Baz(boo: Boolean, bar: Int)

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
  */
