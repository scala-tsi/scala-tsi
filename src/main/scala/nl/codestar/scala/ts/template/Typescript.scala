package nl.codestar.scala.ts.template

import play.twirl.api._
import scala.collection.immutable

class Typescript private (elements: immutable.Seq[Typescript], text: String)
    extends BufferedContent[Typescript](elements, text) {
  def contentType = MimeTypes.TEXT
}

object Typescript {
  def apply(text: String): Typescript = new Typescript(Nil, Formats.safe(text))
  def apply(elements: immutable.Seq[Typescript]) = new Typescript(elements, "")
}

object TypescriptFormat extends Format[Typescript] {
  def raw(text: String): Typescript = Typescript(text)
  def escape(text: String): Typescript = Typescript(text)

  def empty = Typescript("")
  def fill(elements: immutable.Seq[Typescript]) = Typescript(elements)
}
