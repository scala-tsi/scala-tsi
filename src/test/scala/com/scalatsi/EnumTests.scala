package com.scalatsi

import TypescriptType._
import com.scalatsi.types.JavaEnum
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class EnumTests extends AnyWordSpec with Matchers {
  object SomeEnum extends Enumeration {
    type SomeEnum = Value
    val O1, O2 = Value
  }

  "Scala enumerations" should {
    "have typescript representation" in {
      val generated = implicitly[TSType[SomeEnum.type]]
      val manual    = TSType.alias[SomeEnum.type]("SomeEnum", TSLiteralString("O1") | TSLiteralString("O2"))
      generated should ===(manual)
    }
  }

  "Java enumerations" should {
    "have typescript representation" in {
      val generated = implicitly[TSType[JavaEnum]]
      val manual    = TSType.alias[JavaEnum]("JavaEnum", TSLiteralString("ABC") | TSLiteralString("DEF") | TSLiteralString("GHI"))
      generated should ===(manual)
    }
  }

}
