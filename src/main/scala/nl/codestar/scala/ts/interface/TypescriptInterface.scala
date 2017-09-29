package nl.codestar.scala.ts.interface

import TypescriptTypeSerializer.emit

case class TypescriptInterface[T](
    imports: Seq[TypescriptInterface[_ <: AnyRef]] = Seq.empty,
    fileName: String = ".", //TODO no default?
    name: String = "", //TODO no default?
    implicit val tsType: TSNamedType[T]
) {

  def content: String = emit[T]
}

case class TypescriptImport(directory: String)
