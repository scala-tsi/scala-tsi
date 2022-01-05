package models

case class DeepNestingTopLevel(
    prop1: String,
    prop2: Nest1
)

case class Nest1(
    prop3: String,
    prop4: Nest2
)

case class Nest2(
    prop5: String,
    prop6: Nest3
)

case class Nest3(
    prop7: String,
    prop8: Nest4
)

case class Nest4(
    prop8: String,
    prop9: Nest5
)

case class Nest5(
    prop10: String,
    prop11: Nest6
)

case class Nest6(
    prop12: String,
    prop13: Nest7
)

case class Nest7(
    prop14: String,
    prop15: Int
)
