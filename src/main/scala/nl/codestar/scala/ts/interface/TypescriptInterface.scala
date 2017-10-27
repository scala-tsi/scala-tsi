package nl.codestar.scala.ts.interface

import TypescriptTypeSerializer.emit

case class TypescriptInterface[T: TSNamedType](
    imports: Seq[TypescriptImport] = Seq.empty,
    fileName: String = ".", //TODO no default?
    name: String = "" //TODO no default?
) {
  require(
    imports.forall(_.fileName != fileName),
    "imports cannot direct to the same file as the interface which imports them")

  def content: String = emit[T]
}

trait TypescriptImport {
  val fileName: String
  val interfaces: Seq[String]
}

case class ES6TypescriptImport(fileName: String, interfaces: Seq[String])
    extends TypescriptImport {
  override def toString: String =
    if (interfaces.size > 4)
      s"import * from '$fileName'"
    else
      s"import { ${interfaces.mkString(", ")} } from '$fileName'"
}

object ES6TypescriptImport {
  def fromInterfaces[T](
      interfaces: Seq[TypescriptInterface[T]]): Seq[TypescriptImport] =
    interfaces
      .groupBy(_.fileName)
      .map {
        case (fileName, interfaces) =>
          ES6TypescriptImport(fileName, interfaces.map(_.name))
      }
      .toSeq
}
