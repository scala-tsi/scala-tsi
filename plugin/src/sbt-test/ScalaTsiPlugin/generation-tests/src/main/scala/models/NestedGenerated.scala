package models;

import com.scalatsi._

case class A(field: String)

case class B(a: A)

case class NestedGenerated(
    direct: A,
    indirect: B,
    inList: Seq[A],
    inDoubleList: Seq[Seq[A]]
)
