package models;

import com.scalatsi._

case class A(field: String)

case class B(a: A)

case class C(field: Int)

case class NestedGenerated(
    direct: A,
    indirect: B,
    inList: Seq[A],
    inDoubleList: Seq[Seq[A]],
    inEither: Either[Int, C],
    inTuple: (A, Int, String),
    deepNested: Either[A, Either[Int, B]]
)
