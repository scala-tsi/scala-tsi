package models

sealed trait Sealed
case class SealedOption1(foo: String) extends Sealed
case class SealedOption2(bar: Int)    extends Sealed
