package models

import nl.codestar.scala.ts.interface.{ DefaultTSTypes, TSIType }
import nl.codestar.scala.ts.interface.TypescriptType._

case class Foo(bar: Bar, bool: Boolean, num: Option[Int], baz: Option[Baz])

case class Bar(value: String)

case class Baz(boo: Boolean, bar: Int)

object Foo extends DefaultTSTypes {
  implicit val barTSI = TSIType.fromCaseClass[Bar]
  implicit val bazTSI = TSIType.fromCaseClass[Baz]
  implicit val fooTSI = TSIType.fromCaseClass[Foo]
}
