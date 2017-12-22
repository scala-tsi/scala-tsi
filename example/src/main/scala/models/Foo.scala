package models

import nl.codestar.scala.ts.interface.{ DefaultTSTypes, TSType }
import nl.codestar.scala.ts.interface.TypescriptType._

case class Foo(bar: Bar, bool: Boolean, num: Option[Int], baz: Option[Baz])

case class Bar(value: String)

case class Baz(boo: Boolean, bar: Int)

object Foo extends DefaultTSTypes {
  implicit val barTSI = TSType.fromCaseClass[Bar]
  implicit val bazTSI = TSType.fromCaseClass[Baz]
  implicit val fooTSI = TSType.fromCaseClass[Foo]
}
