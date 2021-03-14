package com.scalatsi

import TypescriptType._

import scala.annotation.nowarn
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

case class Person(name: String, age: Int)

class MacroTests extends AnyFlatSpec with Matchers with DefaultTSTypes {
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

    val tsA: TSIType[A] = TSType.fromCaseClass

    TSType.fromCaseClass[B] shouldBe TSType.interface("IB", "a" -> tsA.get)
  }

  "The sealed trait/class to Typescript type macro" should "handle sealed traits" in {
    sealed trait FooOrBar
    case class Foo(foo: String) extends FooOrBar
    case class Bar(bar: Int)    extends FooOrBar

    val foo = TSType.fromCaseClass[Foo].get
    val bar = TSType.fromCaseClass[Bar].get

    TSType.fromSealed[FooOrBar] shouldBe TSType.alias(
      "FooOrBar",
      TSTypeReference("IFoo", Some(foo), Some("Foo")) | TSTypeReference("IBar", Some(bar), Some("Bar"))
    )
  }

  it should "handle sealed abstract classes" in {
    @nowarn("cat=unused-params") sealed abstract class FooOrBar(tpe: String)
    case class Foo(foo: String) extends FooOrBar("Foo")
    case class Bar(bar: Int)    extends FooOrBar("Bar")

    import dsl._
    implicit val tsFoo = TSType.fromCaseClass[Foo] + ("type" -> "Foo")
    implicit val tsBar = TSType.fromCaseClass[Bar] + ("type" -> "Bar")

    tsFoo shouldBe TSType.interface("IFoo", "foo" -> TSString, "type" -> TSLiteralString("Foo"))
    tsBar shouldBe TSType.interface("IBar", "bar" -> TSNumber, "type" -> TSLiteralString("Bar"))

    TSType.fromSealed[FooOrBar] shouldBe TSType.alias(
      "FooOrBar",
      TSTypeReference("IFoo", Some(tsFoo.get), Some("Foo")) | TSTypeReference("IBar", Some(tsBar.get), Some("Bar"))
    )
  }

  it should "handle sealed traits with a non-named mapping" in {
    sealed trait FooOrBar
    case class Foo(foo: String) extends FooOrBar
    case class Bar(bar: Int)    extends FooOrBar

    implicit val tsFoo = TSType.fromCaseClass[Foo]
    implicit val tsBar = TSType.sameAs[Bar, Int]

    tsFoo shouldBe TSType.interface("IFoo", "foo" -> TSString)
    tsBar.get shouldBe TSNumber

    TSType.fromSealed[FooOrBar] shouldBe TSType.alias("FooOrBar", TSTypeReference("IFoo", Some(tsFoo.get), Some("Foo")) | TSNumber)
  }

  it should "handle sealed traits with recursive definitions" in {
    sealed trait LinkedList
    case object Nil                                     extends LinkedList
    case class Node(value: Int, next: LinkedList = Nil) extends LinkedList

    @nowarn("cat=unused") implicit val nilType: TSType[Nil.type] = TSType(TSNull)
    implicit val llType: TSType[Node]                            = TSType.alias("INode", TSNull | TSTypeReference("ILinkedList"))

    TSType.fromSealed[LinkedList] shouldBe TSType.alias("LinkedList", TSNull | TSTypeReference("INode", Some(llType.get), Some("Node")))
  }

  it should "handle sealed traits without subclasses" in {
    sealed trait Empty

    (TSType.fromSealed[Empty]: @nowarn()) shouldBe TSNamedType[Empty](TSAlias("IEmpty", TSNever))
  }

  it should "handle sealed traits with a single subclass" in {
    sealed trait Single
    case class A(foo: Int) extends Single

    val a = TSType.fromCaseClass[A].get

    TSType.fromSealed[Single] shouldBe TSType.alias("Single", TSTypeReference("IA", Some(a)))
  }

  "TSType.getOrGenerate" should "use available implicit if in scope" in {
    case class A(foo: String)

    implicit val tsA = TSType[A](TSNumber)

    TSType.getOrGenerate[A] shouldBe theSameInstanceAs(tsA)
  }

  it should "use available implicit TSNamedType if in scope" in {
    case class A(foo: String)

    import dsl._
    @nowarn("cat=unused") implicit val tsA: TSNamedType[A] = TSType.fromCaseClass[A] + ("type" -> "A")

    TSNamedType.getOrGenerate[A] shouldBe TSType.interface(
      "IA",
      "foo"  -> TSString,
      "type" -> TSLiteralString("A")
    )
  }

  it should "use case class generator for case classes" in {
    case class A(foo: String)

    val generated     = TSType.getOrGenerate[A]
    val fromCaseClass = TSType.fromCaseClass[A]

    generated shouldBe fromCaseClass
    fromCaseClass shouldBe TSType.interface("IA", "foo" -> TSString)
  }

  it should "use sealed trait generator for sealed traits" in {
    sealed trait A
    case class B(foo: String) extends A

    val generated  = TSType.getOrGenerate[A]
    val fromSealed = TSType.fromSealed[A]
    val b          = TSType.fromCaseClass[B].get

    generated shouldBe fromSealed
    fromSealed shouldBe TSType.alias("A", TSTypeReference("IB", Some(b)))
  }

  it should "give a compile error for unsupported types if no implicit is available" in {
    @nowarn("cat=unused") class A

    "TSType.getOrGenerate[A]" shouldNot compile
  }

  it should "not crash on recursive definitions" in {
    case class RecursiveA(b: RecursiveB)
    case class RecursiveB(a: RecursiveA)

    // Check that this generates the expected compile error, but not crashes the compilation
    "TSType.getOrGenerate[RecursiveA]" shouldNot compile
  }

  it should "give an error on non-abstract sealed class" in {
    @nowarn("cat=unused") sealed class Something {}

    "TSType.getOrGenerate[Something]" shouldNot compile
  }

  "TSIType.getOrGenerate" should "use available implicit if in scope" in {
    case class A(foo: String)

    implicit val tsA: TSIType[A] = TSType.interface("Hi", "bar" -> TSNumber)

    TSType.getOrGenerate[A] shouldBe theSameInstanceAs(tsA)
  }

  it should "use case class generator for case classes" in {
    case class A(foo: String)

    val generated     = TSIType.getOrGenerate[A]
    val fromCaseClass = TSType.fromCaseClass[A]

    generated shouldBe fromCaseClass
    fromCaseClass shouldBe TSType.interface("IA", "foo" -> TSString)
  }

  it should "give a compile error for unsupported types if no implicit is available" in {
    @nowarn("cat=unused") sealed trait A

    "TSIType.getOrGenerate[A]" shouldNot compile
  }
}
