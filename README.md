# Scala-TSI

[![Maven Central](https://img.shields.io/maven-central/v/nl.codestar/scala-tsi_2.12.svg)](https://mvnrepository.com/artifact/nl.codestar/scala-tsi)
[![CircleCI](https://img.shields.io/circleci/project/github/code-star/scala-tsi/master.svg)](https://circleci.com/gh/code-star/scala-tsi/)

Scala TSI can automatically generate Typescript Interfaces from your Scala classes.

## Installation

To use the project add the SBT plugin dependency in `project/plugins.sbt`:

```scala
addSbtPlugin("nl.codestar" % "sbt-scala-tsi" % "0.1.2")
```

And configure the plugin in your project:
```scala
// Replace with your project definition
lazy val root = (project in file("."))
    .settings(
      // The classes that you want to generate typescript interfaces for
      typescriptClassesToGenerateFor := Seq("MyClass"),
      // The output file which will contain the typescript interfaces
      typescriptOutputFile := baseDirectory.value / "model.ts",
      // Include the package(s) of the classes here, and make sure to import your typescript conversions
      typescriptGenerationImports := Seq("mymodel._", "MyTypescript._")
    )
```

Now `sbt generateTypescript` will transform a file like
```scala
package mymodel
import nl.codestar.scalatsi._

case class MyClass(foo: String, bar: Int)

object MyTypescript {
  implicit val myClassTs = TSType.fromCaseClass[MyClass]
}
```

Into a typescript interface like
```typescript
export interface MyClass {
  a: string
  b: number
}
```

See [Example](#Example) for more a more in-depth example

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
```scala
package myproject

case class Person(name: String, email: Email, age: Option[Int])
// This type will get erased when serializing to JSON
case class Email(address: String)
```

With [Typescript](https://www.typescriptlang.org/), your frontend can know what data is available in what format.
However, keeping the Typescript definitions in sync with your scala classes is a pain and error-prone. scala-tsi solves that.


First we define the mapping as follows
```scala
package myproject

import nl.codestar.scalatsi._

// A TSType[T] is what tells scala-tsi how to convert your type T into typescript
// MyModelTSTypes contains all TSType[_]'s for your model
// You can also spread these throughout your codebase, for example in the same place where your JSON (de)serializers
object MyModelTSTypes extends DefaultTSTypes {
 
  // Tell scala-tsi to use the typescript type of string whenever we have an Email type
  // Alternatively, TSType.alias[Email, String] will create a `type Email = string` entry in the typescript file
  implicit val tsEmail = TSType.sameAs[Email, String]
  
  // TSType.fromCaseClass will convert your case class to a typescript definition
  implicit val tsPerson = TSType.fromCaseClass[Person]
}
```

And in your build.sbt configure the sbt plugin to output your class:
```
lazy val root = (project in file("."))
  .settings(
    typescriptClassesToGenerateFor := Seq("Person"),
    typescriptGenerationImports := Seq("myproject._", "MyModelTSTypes._"),
    typescriptOutputFile := baseDirectory.value / "model.ts"
  )
```

this will generate in your project root a `model.ts`:
```
export interface IPerson {
  name : string,
  email : string,
  age ?: number
}
```

## Usage

[This document](doc/workings.md) contains more detailed explanation of the library and usage

## Isn't there already [Scala TS](https://github.com/miloszpp/scala-ts)?

Scala TS is good if it can cover your usage-case, but it functionality is very limited.
Check out [this comparison](doc/scala-ts.md) 

## Features

See [this issue](https://github.com/code-star/scala-ts-interfaces/issues/1) for an overview of completed and planned features
