package models

import nl.codestar.scalatsi.DefaultTSTypes
import nl.codestar.scalatsi.TypescriptType._
import nl.codestar.scalatsi.TSType

case class Foo(bar: Bar, bool: Boolean, num: Option[Int], baz: Option[Baz])

case class Bar(value: String)

case class Baz(boo: Boolean, bar: Int)
