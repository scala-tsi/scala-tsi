package nl.codestar.scala.ts.interface

import org.scalatest.{FlatSpec, Matchers}
import nl.codestar.scala.ts.interface.TypescriptType._
import nl.codestar.scala.ts.interface.dsl._

case class Person(name: String, age: Int)

class MacroTests extends FlatSpec with Matchers with DefaultTSTypes {
  // Scala 2.11.11 (maybe others) give false positive unused warnings if a class is used only as a generic
  def ignoreUnused(o: Object) = ()

  "The case class to TypeScript type macro" should "be able to translate a simple case class" in {
    case class Person(name: String, age: Int)
    ignoreUnused(Person("", 1))
    TSIType.fromCaseClass[Person] shouldBe tsInterface("IPerson",
                                                       "name" -> TSString,
                                                       "age" -> TSNumber)
  }

  it should "handle optional types" in {
    case class TestOptional(opt: Option[Long])
    ignoreUnused(TestOptional(None))
    TSIType.fromCaseClass[TestOptional] shouldBe tsInterface(
      "ITestOptional",
      "opt" -> TSUnion.of(TSNumber, TSUndefined))
  }

  it should "handle nested definitions" in {
    case class A(foo: Boolean)
    case class B(a: A)

    ignoreUnused(B(A(false)))

    implicit val tsA: TSIType[A] = TSIType.fromCaseClass

    TSIType.fromCaseClass[B] shouldBe tsInterface("IB", "a" -> tsA.get)
  }

  it should "not compile if a nested definition is missing" in {

    """{
       case class A(foo: Boolean)
       case class B(a: A)
       TSIType.fromCaseClass[B]
    }""".stripMargin shouldNot compile
  }
}
