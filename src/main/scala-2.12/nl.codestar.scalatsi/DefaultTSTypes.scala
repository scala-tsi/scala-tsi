package nl.codestar.scalatsi

import com.scalatsi.{CollectionTSTypes, DefaultTSTypes, JavaTSTypes, PrimitiveTSTypes, ScalaTSTypes, TupleTSTypes}

/** The default Scala-to-Typescript mappings provided by this library */
// Cannot be put in the scala-agnostic code because 2.12 does not support literal types
trait DefaultTSTypes extends PrimitiveTSTypes with ScalaTSTypes with CollectionTSTypes with TupleTSTypes with JavaTSTypes {}
object DefaultTSTypes extends DefaultTSTypes