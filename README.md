# Scala-TSI

[![Maven Central](https://img.shields.io/maven-central/v/nl.codestart/scala-tsi.svg)](https://mvnrepository.com/artifact/nl.codestar/scala-tsi)
[![CircleCI](https://img.shields.io/circleci/project/github/code-star/scala-tsi.svg)](https://circleci.com/gh/code-star/scala-tsi/)

Scala TSI can automatically generate Typescript Interfaces from your Scala classes.

## Installation

To use the project add the SBT plugin dependency in `project/plugins.sbt`:

```scala
addSbtPlugin("nl.codestar" % "sbt-scala-tsi" % "0.1")
```

And enable the plugin on projects using:

```scala
// Replace with your project definition
lazy val root = (project in file("."))
    // This enables the plugin on your project
    .enablePlugins(TypescriptGenPlugin)
```

## Configuration


## Example
Consider this domain model:
```
// This type will get erased when serializing to JSON
case class Email(address: String)
case class Person(name: String, email: Email, age: Option[Int])

```

You can define the typescript mapping as follows:
```
import nl.codestar.scala.ts.interface._
import nl.codestar.scala.ts.interface.dsl._
import DefaultTSTypes._

implicit val tsEmail: TSType[Email] = tsAlias[Email, String]("Email")
// If you rather not have a "Email" type alias, use:
// implicit val tsEmail: TSType[Email] = TSType.of[String]
implicit val tsPerson: TSIType[Person] = TSIType.fromCaseClass

// TODO: how do people actually emit the files?
val interfaces: String = emit[Person]
```

this will generate:
```
interface IPerson {
  name: string,
  email: Email,
  age?: number
}

type Email = string
```

## Installation
TODO

## Usage
TODO

## Documentation
TODO (won't ever get done)

## Comparison to scala-ts

Explain why the scala-ts approach isn't viable for us, because it's fairly limited and doesn't allow control over types. Plus it generated optional members as `member: T | null`, that alone makes it clearly unviable for everything all the time.

## Features

See [this issue](https://github.com/code-star/scala-ts-interfaces/issues/1)
