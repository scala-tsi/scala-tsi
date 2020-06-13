package nl.codestar

import com.{scalatsi => newname}

/** @deprecated Renamed to com.scalatsi */
package object scalatsi {
  @deprecated("0.3.0", "Use com.scalatsi.TSType")
  type TSType[T] = newname.TSType[T]
  @deprecated("0.3.0", "Use com.scalatsi.TSType")
  final val TSType = newname.TSType
  @deprecated("0.3.0", "Use com.scalatsi.TSNamedType")
  type TSNamedType[T] = newname.TSNamedType[T]
  @deprecated("0.3.0", "Use com.scalatsi.TSNamedType")
  final val TSNamedType = newname.TSNamedType
  @deprecated("0.3.0", "Use com.scalatsi.TSIType")
  type TSIType[T] = newname.TSIType[T]
  @deprecated("0.3.0", "Use com.scalatsi.TSIType")
  final val TSIType = newname.TSIType
  @deprecated("0.3.0", "Use com.scalatsi.TypescriptType")
  type TypescriptType = newname.TypescriptType
  @deprecated("0.3.0", "Use com.scalatsi.TypescriptType")
  final val TypescriptType = newname.TypescriptType
  @deprecated("0.3.0", "Use com.scalatsi.TypescriptTypeSerializer")
  final val TypescriptTypeSerializer = newname.TypescriptTypeSerializer
  @deprecated("0.3.0", "Use com.scalatsi.DefaultTSTypes")
  type DefaultTSTypes = newname.DefaultTSTypes
  @deprecated("0.3.0", "Use com.scalatsi.DefaultTSTypes")
  final val DefaultTSTypes = newname.DefaultTSTypes
}
