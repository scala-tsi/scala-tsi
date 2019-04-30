## Comparison to scala-ts

[scala-ts](https://github.com/miloszpp/scala-ts) is another tool to generate typescript interfaces from your scala domain code.
When we evaluated it for use, but encountered fundamental limitations making it unsuitable for use in our projects.

##### scala-ts is limited to the types it knows and case classes.

For example, the current version (0.4.0) cannot handle `BigDecimal`.
If your domain contains a `case class Foo(num: BigDecimal)` you are out of luck with scala-ts.
For built-in Java and Scala this can be expanded, but it will not work for your own types which aren't case classes or types that come from a library you depend on. 

##### scala-ts only has a single way to generate case classes

Sometimes, you want to modify your JSON (de)serialization slightly.
This is not possible with scala-ts, but scala-tsi can handle this.

Say you want to remove the `password` property of your `User` class in serialization:
```
case class User(name: String, password: String)

// Example play json writer
implicit val userSerializer = Json.writes[User].transform(jsobj => jsobj - "password")
```

This is not possible with scala-ts, but can be done in scala-tsi as follows:

```
import nl.codestar.scala.ts.interface.dsl._

implicit val userTsType = TSType.fromCaseClass[User] - "password"
```

##### However, this flexibility comes at increased complexity

scala-ts is *much* simpler to use.
If your project can work within the limitations of scala-ts, we recommend you use scala-ts.
