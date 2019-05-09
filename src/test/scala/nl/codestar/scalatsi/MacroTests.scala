package nl.codestar.scalatsi

import org.scalatest.{FlatSpec, Matchers}
import nl.codestar.scalatsi.TypescriptType._

case class Person(name: String, age: Int)

class MacroTests extends FlatSpec with Matchers with DefaultTSTypes {
  "The case class to TypeScript type macro" should "be able to translate a simple case class" in {
    case class Person(name: String, age: Int)
    TSType.fromCaseClass[Person] shouldBe TSType.interface("IPerson", "name" -> TSString, "age" -> TSNumber)
  }

  it should "handle optional types" in {
    case class TestOptional(opt: Option[Long])
    TSType.fromCaseClass[TestOptional] shouldBe TSType.interface("ITestOptional", "opt" -> TSUnion.of(TSNumber, TSUndefined))
  }

  it should "handle nested definitions" in {
    case class A(foo: Boolean)
    case class B(a: A)

    implicit val tsA: TSIType[A] = TSType.fromCaseClass

    TSType.fromCaseClass[B] shouldBe TSType.interface("IB", "a" -> tsA.get)
  }

  it should "handle sealed traits" in {
    sealed trait FooOrBar
    case class Foo(foo: String) extends FooOrBar
    case class Bar(bar: Int)    extends FooOrBar

    import nl.codestar.scalatsi.dsl._
    implicit val tsFoo = TSType.fromCaseClass[Foo] + ("type" -> "Foo")
    implicit val tsBar = TSType.fromCaseClass[Bar] + ("type" -> "Bar")

    implicit val tsFooOrBar = TSType.fromSealedTrait[FooOrBar]

    tsFoo.get shouldBe TSType.interface("IFoo", "foo" -> TSString, "type" -> TSLiteralString("Foo"))
    tsFoo.get shouldBe TSType.interface("IBar", "bar" -> TSNumber, "type" -> TSLiteralString("Bar"))

    tsFooOrBar shouldBe TSType.alias("FooOrBar", TSExternalName("Foo") | TSExternalName("Bar"))
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
