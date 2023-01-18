package com.scalatsi

/** The default Scala-to-Typescript mappings provided by this library */
// Cannot be put in the scala-agnostic code because 2.12 does not support literal types
trait DefaultTSTypes
    extends ScalaTSTypes
    with CollectionTSTypes
    with FunctionTSTypes
    with TupleTSTypes
    with JavaTSTypes
    with LiteralTSTypes
    with ScalaEnumTSTypes
    with ScalaVersionSpecificTypes {}
object DefaultTSTypes extends DefaultTSTypes
