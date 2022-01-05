package com.scalatsi.dsl

import com.scalatsi.dsl.CommonTypes.JsonValue
import com.scalatsi.TypescriptTypeSerializer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CommonTypesTests extends AnyFlatSpec with Matchers {
  "JSON Types" should "be serialized correctly" in {
    val serialized = TypescriptTypeSerializer.emits(JsonValue)
    serialized should equal(
      s"""export type JsonValue = (string | number | boolean | null | JsonValue[] | { [ member: string ]: JsonValue })\n"""
    )
  }
}
