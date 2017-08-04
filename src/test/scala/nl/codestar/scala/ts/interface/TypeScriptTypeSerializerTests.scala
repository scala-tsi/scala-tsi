package nl.codestar.scala.ts.interface

import org.scalatest.{FlatSpec, Matchers}
import DefaultTSTypes._

class TypeScriptTypeSerializerTests extends FlatSpec with Matchers {

  "Typescript type serializer" should "be able to generate typescript for a simple interface" in {

    implicit val personTsWrites: TSType[Person] = Typescript.writes

    val x = TypescriptTypeSerializer.emit[Person]

    x.trim should be("""
        |interface Person {
        |  name: string
        |  age: number
        |}
      """.stripMargin.trim)
  }
}

case class Person(name: String, age: Int)
