package com.scalatsi

import TypescriptType._
import org.scalatest.{Matchers, WordSpec}
import com.scalatsi.types.JavaEnum

class EnumTests extends WordSpec with Matchers with DefaultTSTypes {

  "Scala enumerations" should {
    "have typescript representation" in {
      object SomeEnum extends Enumeration {
        type SomeEnum = Value
        val O1, O2 = Value
      }

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
