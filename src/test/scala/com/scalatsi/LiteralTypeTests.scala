package com.scalatsi

import TypescriptType.{TSLiteralBoolean, TSLiteralNumber, TSLiteralString}
import org.scalactic.source
import org.scalatest.matchers.should.Matchers
import org.scalatest.verbs.StringVerbBlockRegistration
import org.scalatest.wordspec.AnyWordSpec

class LiteralTypeTests extends AnyWordSpec with Matchers {

  "Default TS Types should be defined" forWord {
    "literal booleans" in {
      TSType.get[true] shouldBe TSType(TSLiteralBoolean(true))
      TSType.get[false] shouldBe TSType(TSLiteralBoolean(false))
    }

    "literal strings" in {
      TSType.get["Hello world!"] shouldBe TSType(TSLiteralString("Hello world!"))
    }

    "literal numbers" in {
      TSType.get[42] shouldBe TSType(TSLiteralNumber(BigDecimal(42)))
      TSType.get[42L] shouldBe TSType(TSLiteralNumber(BigDecimal(42L)))
      TSType.get[42.0] shouldBe TSType(TSLiteralNumber(BigDecimal(42.0)))
      TSType.get[42.0f] shouldBe TSType(TSLiteralNumber(BigDecimal(42.0f)))
    }
  }

  "Literal numbers" should {
    "work in other types" in {
      case class Literal(
          a: "Hello!",
          b: 42,
          c: true
      )

      implicit val tsType = TSType.fromCaseClass[Literal]

      TypescriptTypeSerializer.emit[Literal]() shouldBe
        """export interface ILiteral {
          |  a: "Hello!"
          |  b: 42
          |  c: true
          |}
          |""".stripMargin
    }
  }

  import scala.language.implicitConversions
  implicit def convertToStringHasWrapperForVerb(o: String)(implicit position: source.Position): HasWrapper =
    new HasWrapper {
      override val leftSideString = o.trim
      override val pos            = position
    }

  trait HasWrapper {
    val leftSideString: String
    val pos: source.Position

    def forWord(right: => Unit)(implicit fun: StringVerbBlockRegistration): Unit = {
      fun(leftSideString, "for", pos, () => right)
    }
  }
}
