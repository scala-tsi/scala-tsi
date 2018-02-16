This section is still a work in progress

scala-tsi has two important types: `TypescriptType` which represents a valid type in Typescript and `TSType`, a typeclass that holds a link from a scala type to a `TypescriptType`.

scala-tsi contains some functionality to print a `TypescriptType` to its typescript representation, for example `TSNumber` will be printed as `number`.

In order to know the typescript representation of any Scala type, you have to define an `implicit` `TSType`.
scala-tsi contains the a `DefaultTSTypes` trait with many definitions for Java and Scala classes and with `TSType.fromCaseClass` you can generate a `TSType` for any case class.
`TSType` contains more factory methods to construct your own mappings, and we have a small DSL which allows you to freely construct types.


