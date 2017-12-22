# Scala-TSI

[![Maven Central](https://img.shields.io/maven-central/v/nl.codestart/scala-tsi.svg)](https://mvnrepository.com/artifact/nl.codestar/scala-tsi)
[![CircleCI](https://img.shields.io/circleci/project/github/code-star/scala-tsi.svg)](https://circleci.com/gh/code-star/scala-tsi/)

Scala TSI can automatically generate Typescript Interfaces from your Scala classes.

## Installation

To use the project add the SBT plugin dependency in `project/plugins.sbt`:

```scala
addSbtPlugin("nl.codestar" % "sbt-scala-tsi" % "0.1.0")
```

And enable the plugin on your project using:

```scala
// Replace with your project definition
lazy val root = (project in file("."))
    // This enables the plugin on your project
    .enablePlugins(TypescriptGenPlugin)
    .settings(
      typescriptClassesToGenerateFor := Seq("MyClass"),
      // Make sure this also imports your own TSType[_] implicits
      typescriptGenerationImports := Seq("mymodel._"),
      typescriptOutputFile := baseDirectory.value / "model.ts"
    )
```

## Configuration

| Key | Type | Description |
| --- | ---- | ----------- |
| typescriptClassesToGenerateFor | Seq[String] | A list of all your (top-level) classes that you want to generate interfaces for |
| typescriptGenerationImports | Seq[String] | A list of all imports. This should import all classes you defined above, as well as all implicit `TSType`'s for those classes |
| typescriptOutputFile | File | The output file with generated typescript interfaces

## Example

You can check out the [example project](example/) for a complete set-up and more examples.

Say we have the following JSON:
```json
{
   "name": "person name",
   "email": "abc@example.org",
   "age": 25
}
```

Generated from this Scala domain model:
```
package myproject

case class Person(name: String, email: Email, age: Option[Int])
// This type will get erased when serializing to JSON
case class Email(address: String)
```

You can define the typescript mapping as follows:
```
package myproject

import nl.codestar.scala.ts.interface._

// MyModelTSTypes contains al TSType[_]'s for my model. You can also spread these throughout your codebase, e.g. next to where you define your JSON serializers
// DefaultsTSTypes contains various default mappings from scala types to typescript types, you can also use import DefaultTSTypes._
object MyModelTSTypes extends DefaultTSTypes {
 
  // TSType.tsAlias[X, Y] will add a `type X = Y` line to the generated typescript
  // Alternatively, TSType.transparent[X, Y] will always replace X with the typescript type of Y in the generated typescript
  implicit val tsEmail = TSType.tsAlias[Email, String]("Email")
  
  // TSType.fromCaseClass will convert your case class to a typescript definition
  implicit val tsPerson = TSType.fromCaseClass[Person]
}
```

And in your build.sbt configure the sbt plugin to output your domain model:
```
lazy val root = (project in file("."))
  .enablePlugins(TypescriptGenPlugin)
  .settings(
    typescriptClassesToGenerateFor := Seq("Person"),
    typescriptGenerationImports := Seq("myproject._", "MyModelTSTypes._"),
    typescriptOutputFile := baseDirectory.value / "model.ts"
  )
```

this will generate in your project root a `model.ts`:
```
interface IPerson {
  name: string,
  email: Email,
  age?: number
}

type Email = string
```

## Scala-TSI explained

This section is still a work in progress

scala-tsi has two important types: `TypescriptType` which represents a valid type in Typescript and `TSType`, a typeclass that holds a link from a scala type to a `TypescriptType`.

scala-tsi contains some functionality to print a `TypescriptType` to its typescript representation, for example `TSNumber` will be printed as `number`.

In order to know the typescript representation of any Scala type, you have to define an `implicit` `TSType`.
scala-tsi contains the a `DefaultTSTypes` trait with many definitions for Java and Scala classes and with `TSType.fromCaseClass` you can generate a `TSType` for any case class.
`TSType` contains more factory methods to construct your own mappings, and we have a small DSL which allows you to freely construct types.

## Comparison to scala-ts

[scala-ts](https://github.com/miloszpp/scala-ts) is another tool to generate typescript interfaces from your scala domain code.
When we evaluated it for use, we found it unsuitable for use in our projects because of the following limitations:

##### scala-ts is limited to the types the authors have provided and case classes.

For example, the current version (0.4.0) cannot handle `BigDecimal`.
If your domain contains a `case class Foo(num: BigDecimal)` you are out of luck with scala-ts.
For built-in java and scala you could theoretically expand this, but the tool will never work with library or your own types which aren't case classes. 

##### scala-ts only has a single way to generate case classes

Sometimes, you want to modify your JSON (de)serialization slightly.
With scala-ts, you are out of luck, but with scala-tsi this is not a problem.

Say you want to remove the `password` property of your `User` class in serialization:
```
case class User(name: String, password: String)

// Example play json writer
implicit val userSerializer = Json.writes[User].transform(jsobj => jsobj - "password")
```

This is not possible with scala-ts, but not a problem with scala-tsi:

```
import nl.codestar.scala.ts.interface.dsl._

implicit val userTsType = TSType.fromCaseClass[User] - "password"
```

##### However, the flexibility of scala-tsi comes at increased complexity

scala-ts is *much* simpler to use.
If your project can work within the limitations of scala-ts, we recommend you use scala-ts.


## Features

See [this issue](https://github.com/code-star/scala-ts-interfaces/issues/1)
