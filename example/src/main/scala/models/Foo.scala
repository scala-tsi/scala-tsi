package models

import com.scalatsi.{DefaultTSTypes, TSType}

case class Foo(bar: Bar, bool: Boolean, num: Option[Int], baz: Option[Baz])

case class Bar(value: String)

case class Baz(boo: Boolean, bar: Int)
