# Scala-TSI

[![CircleCI](https://img.shields.io/circleci/project/github/code-star/scala-tsi/master.svg)](https://circleci.com/gh/code-star/scala-tsi/)


[![2.12](https://img.shields.io/maven-central/v/nl.codestar/scala-tsi_2.12.svg?label=2.12)](https://mvnrepository.com/artifact/nl.codestar/scala-tsi)
[![2.13](https://img.shields.io/maven-central/v/nl.codestar/scala-tsi_2.13.svg?label=2.13)](https://mvnrepository.com/artifact/nl.codestar/scala-tsi)

Scala TSI can automatically generate Typescript Interfaces from your Scala classes.

## Installation

To use the project add the SBT plugin dependency in `project/plugins.sbt`:

```scala
// See badge above for latest version number
addSbtPlugin("nl.codestar" % "sbt-scala-tsi" % "0.2.0")
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
      // Include the package(s) of the classes here
      // Optionally import your own TSType implicits to override default default generated
      typescriptGenerationImports := Seq("mymodel._", "MyTypescript._")
    )
```

Now `sbt generateTypescript` will transform a file like
```scala
case class MyClass(foo: String, bar: Int)
```

Into a typescript interface like
```typescript
export interface IMyClass {
  foo: string
  bar: number
}
```

See [#Example](#Example) or [the example project](example/) for more a more examples

## Configuration

| Key | Type | Default | Description |
| --- | ---- | ------- | ----------- |
| typescriptClassesToGenerateFor | Seq[String] | `Seq()` | A list of all your (top-level) classes that you want to generate interfaces for |
| typescriptGenerationImports | Seq[String] | `Seq()` | A list of all imports. This should import all classes you defined above, as well as custom `TSType` implicits |
| typescriptOutputFile | File | `target/scala-interfaces.ts`| The output file with generated typescript interfaces |
| typescriptStyleSemicolons | Boolean | `false` | Whether to add booleans to the exported model (experimental) |

## Example

You can check out the [example project](example/) for a complete set-up and more examples.

Say we have the following JSON:
```json
{
   "name": "person name",
   "email": "abc@example.org",
   "age": 25,
   "job": {
      "tasks": ["Be in the office", "Drink coffee"],
      "boss": "Johnson"
   }
}
```

Generated from this Scala domain model:
```scala
package myproject

case class Person(
  name: String,
  email: Email,
  age: Option[Int],
  // for privacy reasons, we do not put this social security number in the JSON
  ssn: Option[Int],
  job: Job
)
// This type will get erased when serializing to JSON, only the string remains
case class Email(address: String)

case class Job(tasks: Seq[String], boss: String)

```

With [Typescript](https://www.typescriptlang.org/), your frontend can know what data is available in what format.
However, keeping the Typescript definitions in sync with your scala classes is a pain and error-prone. scala-tsi solves that.

First we define the mapping as follows
```scala
package myproject

import nl.codestar.scalatsi._
import nl.codestar.scalatsi.dsl._

// A TSType[T] is what tells scala-tsi how to convert your type T into typescript
// MyModelTSTypes contains all TSType[_]'s for your model
// You can also spread these throughout your codebase, for example in the same place where your JSON (de)serializers
object MyModelTSTypes extends DefaultTSTypes {
 
  // Tell scala-tsi to use the typescript type of string whenever we have an Email type
  // Alternatively, TSType.alias[Email, String] will create a `type Email = string` entry in the typescript file
  implicit val tsEmail = TSType.sameAs[Email, String]
  
  // TSType.fromCaseClass will convert your case class to a typescript definition
  // `- ssn` indicated the ssn field should be removed
  implicit val tsPerson = TSType.fromCaseClass[Person] - "ssn"
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
  name : string
  email : string
  age ?: number
  job: IJob
}

export interface IJob {
  tasks: string[]
  boss: string
}
```

## Usage

[This document](doc/workings.md) contains more detailed explanation of the library and usage

## Features

See [this list](https://github.com/code-star/scala-ts-interfaces/issues/1) for an overview of completed features, and issues for open feature ideas.
