package nl.codestar.scalatsi

import org.scalatest.{Matchers, WordSpec}
import com.scalatsi.convertToStringHasWrapperForVerb

import scala.annotation.nowarn

@nowarn("cat=deprecation")
class OldNameTests extends WordSpec with Matchers with DefaultTSTypes {
  "Old names should still compile" forWord {
    "String" in { "implicitly[TSType[String]]" should compile }
    "dsl" in {
      """
         import nl.codestar.scalatsi.dsl._
         case class Foo(bar: String)
         val first: TSIType[Foo] = TSType.fromCaseClass[Foo]
         val second: TSIType[Foo] =
         TSType.fromCaseClass[Foo] + ("extraField" -> classOf[String])
      """ should compile
    }
  }
}
