package nl.codestar.scalatsi

import org.scalatest.{FlatSpec, Matchers}
import nl.codestar.scalatsi.TypescriptType._

case class Person(name: String, age: Int)

class MacroTests extends FlatSpec with Matchers with DefaultTSTypes {
  // Scala 2.11.11 (maybe others) give false positive unused warnings if a class is used only as a generic parameter
  def ignoreUnused(o: Object) = ()

  "The case class to TypeScript type macro" should "be able to translate a simple case class" in {
    case class Person(name: String, age: Int)
    ignoreUnused(Person("", 1))
    TSType.fromCaseClass[Person] shouldBe TSType.interface("IPerson", "name" -> TSString, "age" -> TSNumber)
  }

  it should "handle optional types" in {
    case class TestOptional(opt: Option[Long])
    ignoreUnused(TestOptional(None))
    TSType.fromCaseClass[TestOptional] shouldBe TSType.interface("ITestOptional", "opt" -> TSUnion.of(TSNumber, TSUndefined))
  }

  it should "handle nested definitions" in {
    case class A(foo: Boolean)
    case class B(a: A)

    ignoreUnused(B(A(false)))

    implicit val tsA: TSIType[A] = TSType.fromCaseClass

    TSType.fromCaseClass[B] shouldBe TSType.interface("IB", "a" -> tsA.get)
  }

  it should "handle sealed traits" in {
    sealed trait AB
    case class A(a: Int) extends AB
    case class B(b: String) extends AB

    // implicit val abType: TSType[AB] = ???
    pending
  }

  it should "handle recursive definitions" in {
    sealed trait LinkedList
    case object Nil extends LinkedList
    case class Node(value: Int, next: LinkedList = Nil)

    // TODO: Determine this automatically: [#35](https://github.com/code-star/scala-tsi/issues/35)
    implicit val llType: TSType[LinkedList] = TSType(TSType.external("INil").get | TSType.external("INode").get)

    TSType.fromCaseClass[Node] shouldBe TSType.interface("INode", "value" -> TSNumber, "next" -> llType.get)
  }

  it should "not compile if a nested definition is missing" in {

    """{
       case class A(foo: Boolean)
       case class B(a: A)
       TSIType.fromCaseClass[B]
    }""".stripMargin shouldNot compile
  }
}
