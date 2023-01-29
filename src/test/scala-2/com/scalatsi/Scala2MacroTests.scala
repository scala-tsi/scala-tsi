package com.scalatsi

import TypescriptType.*

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

// Tests for the macro that are not (yet) supported by the scala 3 macro
class Scala2MacroTests extends AnyFlatSpec with Matchers {
  "The case class to TypeScript type macro" should "handle nested polymorphic members " in {
    case class Element(foo: String)
    case class Root(
        twoLevels: Seq[Seq[Element]],
        threeLevels: Seq[Seq[Seq[Element]]],
        branched: Either[String, Either[Int, Seq[Element]]]
    )

    val tsElement: TypescriptType = TSType.fromCaseClass[Element].get

    TSType.fromCaseClass[Root] shouldBe TSType.interface(
      "IRoot",
      "twoLevels"   -> tsElement.array.array,
      "threeLevels" -> tsElement.array.array.array,
      "branched"    -> (TSString | TSNumber | tsElement.array)
    )
  }

  it should "handle polymorphic members with parameter type that is itself generated" in {
    case class Element(foo: String)
    case class Root(
        listField: Seq[Element],
        eitherField: Either[String, Element],
        tuple3Field: (Element, String, Int)
    )

    val tsElement: TypescriptType = TSType.fromCaseClass[Element].get

    TSType.fromCaseClass[Root] shouldBe TSType.interface(
      "IRoot",
      "listField"   -> tsElement.array,
      "eitherField" -> (TSString | tsElement),
      "tuple3Field" -> TSTuple.of(tsElement, TSString, TSNumber)
    )
  }
}
