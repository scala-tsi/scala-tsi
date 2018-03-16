# scala-tsi usage

### TSType and TypescriptType

scala-tsi has two important types

 1. `TypescriptType` which represents a valid type in Typescript. <br />
     It is an [AST](https://en.wikipedia.org/wiki/Abstract_syntax_tree) of a typescript type. 
 2. `TSType`, a [Type class](https://blog.scalac.io/2017/04/19/typeclasses-in-scala.html) that holds a link from a scala type to a `TypescriptType`. <br />
     A `TSType[T]` tells us how to transform `T` into a `TypescriptType`. <br />
     It fills a similar function as a `play-json` `Writes[T]` or a `circe` `Encoder[T]`, which tell how to transform an instance of a `T` into json. 

scala-tsi contains some functionality to transform a `TypescriptType` into valid Typescript. <br />
This table contains some examples of the different representations:

| Scala Type | `TypescripType` | Typescript |
| ---------- | --------------- | ---------- |
| `Int`      | `TSNumber`      | `number`   |
| `Double`   | `TSNumber`      | `number`   |
| `Either[String,Int]` | `TSUnion(TSString, TSNumber)` | `string | number` |
| `case class A(foo: Int)`  | `TypescriptInterface("A", Map("foo" -> TSNumber))` | `interface A { foo: number }` |
| [`42.type`](http://docs.scala-lang.org/sips/pending/42.type.html) | `TSLiteralNumber(42)` | `42` |

### Creating `TSType`s

The link in the above table from scala type to `TypescriptType` is provided by an implicit `TSType`. <br />
You will have to define these yourself and put these in scope.

Of course scala-tsi helps you with this. The `DefaultTSTypes` trait and object contain a large number of definitions for built-in Java and Scala classes, e.g. it contains a `TSType[Int]`.
For your own type the simplest case is a case class, you can use `implicit val myCaseClassTSType = TSType.fromCaseClass[MyCaseClass]` to generate a `TSType[MyCaseClass]` which scala-tsi can use to output your classes. 
There are other ways to construct `TSType` defined in the companion object which we encourage you to check out. Most interesting are `TSType.fromCaseClass`, `TSType.alias` and `TSType.interface`.

scala-tsi also defines a small DSL which you can use to construct custom definitions

```scala
import nl.codestar.scalatsi._
import nl.codestar.scalatsi.dsl._
import DefaultTSTypes._

// Includes implicit conversion from literal values to literal typescript types
val literal42: TypescriptType = 42

// Link the scala type Foo to an typescript type "foo" that already exists and we don't define
implicit val fooTSType = TSType.external[Foo]("foo")

// Using TSType.interface you can construct your own interface
implicit val myClassTSType: TSType[MyClass] = TSType.interface("MyClass",
  // Classes will automatically be converted to the typescriptType
  "foo" -> classOf[Foo],
  // Tuples will be converted into arrays of specific length and type
  "tuple3" -> classOf[(Int, String, Foo)], 
  // You can create unions with the | operator defined on TSType and TypescriptType
  "numberOrString" -> (TSNumber | TSString),
  // You can create an array of a type with .array
  "stringArray" -> TSString.array,
  // literal types
  "fortyTwo" -> 42
)

// Output:
/*
interface MyClass {
  foo: Foo
  tuple3: [number, string, Foo]
  numberOrString: number | string
  stringArray: string[]
  fortyTwo: 42
}
*/

// Use +, ++, - and -- to modify existing TSIType's or TSInterface's
implicit val myCaseClassWithExtraField = TSType.fromCaseClass[myCaseClass] + ("foo" -> "bar")

```

### Converting `TypescriptType`s to valid typescript

Normally the SBT plugin will output Typescript for you.
However, you can also do this yourself using `TypescriptTypeSerializer`.

`TypescriptTypeSerializer.serialize` will transform a single type into typescript, e.g. `serialize(TSNumber) == "number"` and `serialize(TSInterface("Foo")) == "Foo"`

`TypescriptTypeSerializer.emits` will output multiple named types, e.g. `emits(TSInterface("Foo")) == "interface Foo {}"`
