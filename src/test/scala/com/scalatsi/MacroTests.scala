package com.scalatsi

import TypescriptType.*

import scala.annotation.{nowarn, unused}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

case class Person(name: String, age: Int)

class MacroTests extends AnyFlatSpec with Matchers {
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

  it should "handle polymorphic members with parameter type that is itself generated" in {
    case class Element(foo: String)
    case class Root(
        listField: Seq[Element],
        setField: Set[Element]
        // eitherField: Either[String, Element],
        // tuple3Field: (Element, String, Int)
    )

    val tsElement: TypescriptType = TSType.fromCaseClass[Element].get

    TSType.fromCaseClass[Root] shouldBe TSType.interface(
      "IRoot",
      "listField"   -> tsElement.array,
      "eitherField" -> (TSString | tsElement),
      "tuple3Field" -> TSTuple.of(tsElement, TSString, TSNumber)
    )
  }

//  it should "handle nested polymorphic members " in {
//    case class Element(foo: String)
//    case class Root(
//        twoLevels: Seq[Seq[Element]],
//        threeLevels: Seq[Seq[Seq[Element]]],
//        branched: Either[String, Either[Int, Seq[Element]]]
//    )
//
//    val tsElement: TypescriptType = TSType.fromCaseClass[Element].get
//
//    TSType.fromCaseClass[Root] shouldBe TSType.interface(
//      "IRoot",
//      "twoLevels"   -> tsElement.array.array,
//      "threeLevels" -> tsElement.array.array.array,
//      "branched"    -> (TSString | TSNumber | tsElement.array)
//    )
//  }

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
    sealed abstract class FooOrBar(@unused tpe: String)
    case class Foo(foo: String) extends FooOrBar("Foo")
    case class Bar(bar: Int)    extends FooOrBar("Bar")

    import dsl.*
    implicit val tsFoo: TSIType[Foo] = TSType.fromCaseClass[Foo] + ("type" -> "Foo")
    implicit val tsBar: TSIType[Bar] = TSType.fromCaseClass[Bar] + ("type" -> "Bar")

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

    implicit val tsFoo: TSIType[Foo] = TSType.fromCaseClass[Foo]
    implicit val tsBar: TSType[Bar]  = TSType.sameAs[Bar, Int]

    tsFoo shouldBe TSType.interface("IFoo", "foo" -> TSString)
    tsBar.get shouldBe TSNumber

    TSType.fromSealed[FooOrBar] shouldBe TSType.alias("FooOrBar", TSTypeReference("IFoo", Some(tsFoo.get), Some("Foo")) | TSNumber)
  }

  it should "handle sealed traits with recursive definitions" in {
    sealed trait LinkedList
    case object Nil                                     extends LinkedList
    case class Node(value: Int, next: LinkedList = Nil) extends LinkedList

    @unused implicit val nilType: TSType[Nil.type] = TSType(TSNull)
    implicit val llType: TSType[Node]              = TSType.alias("INode", TSNull | TSTypeReference("ILinkedList"))

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

  it should "handle case classes with polymorphic custom types inside" in {
    class CustomTsType
    case class ContainsGeneric(foos: Seq[CustomTsType])

    @unused implicit val tsFoo: TSType[CustomTsType] = TSType(TSString)

    TSType.fromCaseClass[ContainsGeneric] shouldBe TSType.interface("IContainsGeneric", "foos" -> TSString.array)
  }

  "TSType.getOrGenerate" should "use available implicit if in scope" in {
    case class A(foo: String)

    implicit val tsA: TSType[A] = TSType[A](TSNumber)

    TSType.getOrGenerate[A] shouldBe theSameInstanceAs(tsA)
  }

  it should "use available implicit TSNamedType if in scope" in {
    case class A(foo: String)

    import dsl.*
    @unused implicit val tsA: TSNamedType[A] = TSType.fromCaseClass[A] + ("type" -> "A")

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
    @unused class A

    "TSType.getOrGenerate[A]" shouldNot compile
  }

  it should "not crash on recursive definitions" in {
    case class RecursiveA(b: RecursiveB)
    case class RecursiveB(a: RecursiveA)

    // Check that this generates the expected compile error, but not crashes the compilation
    "TSType.getOrGenerate[RecursiveA]" shouldNot compile
  }

  it should "give an error on non-abstract sealed class" in {
    @unused sealed class Something {}

    "TSType.getOrGenerate[Something]" shouldNot compile
  }

  it should "support custom polymorphic types in the same scope" in {
    class CustomInPolymorphic

    @unused implicit val customTsType: TSType[CustomInPolymorphic] = TSType(TSString)
    TSType.getOrGenerate[Seq[CustomInPolymorphic]] shouldBe TSType(TSString.array)
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
    @unused sealed trait A

    "TSIType.getOrGenerate[A]" shouldNot compile
  }
}
