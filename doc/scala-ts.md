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
