package com.scalatsi.typescriptype

import com.scalatsi.TypescriptType.{TSAny, TSBoolean, TSNumber, TSString, TSUnion}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TSUnionTests extends AnyFlatSpec with Matchers {
  "TypescriptType.|" should "create union" in {
    (TSBoolean | TSNumber) shouldBe TSUnion.of(TSBoolean, TSNumber)
  }

  it should "add to union" in {
    TSUnion.of(TSBoolean, TSNumber) | TSString shouldBe TSUnion.of(TSBoolean, TSNumber, TSString)
  }

  it should "flatten union" in {
    TSBoolean | TSUnion.of(TSNumber) shouldBe TSUnion.of(TSBoolean, TSNumber)
  }

  "TSUnion.flatten" should "flatten a union type" in {
    val union = TSUnion.of(TSUnion.of(TSUnion.of(TSAny)), TSString, TSUnion.of(TSNumber, TSBoolean))
    union.flatten shouldBe TSUnion.of(TSAny, TSString, TSNumber, TSBoolean)
  }
}
