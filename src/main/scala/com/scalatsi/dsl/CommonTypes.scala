package com.scalatsi.dsl

import com.scalatsi.TypescriptType
import com.scalatsi.TypescriptType.{
  TSAlias,
  TSBoolean,
  TSIndexedInterface,
  TSNull,
  TSNumber,
  TSString,
  TSTypeReference,
  TypescriptNamedType
}

/** Typescript types for things that are not directly have a JVM type, but are useful nonetheless */
object CommonTypes {
  private val JsonValueRef                 = TSTypeReference("JsonValue")
  final val JsonPrimitive: TypescriptType  = TSString | TSNumber | TSBoolean | TSNull
  final val JsonObject: TypescriptType     = TSIndexedInterface("member", TSString, JsonValueRef)
  final val JsonValue: TypescriptNamedType = TSAlias(JsonValueRef.name, JsonPrimitive | JsonValueRef.array | JsonObject)
}
