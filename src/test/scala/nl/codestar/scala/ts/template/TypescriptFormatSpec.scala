package nl.codestar.scala.ts.template

import org.scalatest.{ MustMatchers, WordSpec }

class TypescriptFormatSpec extends WordSpec with MustMatchers {

  "Typescript format" should {
    "show null text values as empty" in {
      val text: String = null

      Typescript(text).body mustBe empty
    }

    "foo" in {
      Typescript()
    }
  }
}
