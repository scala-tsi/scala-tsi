package com.scalatsi

case class Inner(i: String)
case class Outer(is: Seq[Inner])

object Outer {
//  implicit val tst: TSType[Inner]  = TSType.fromCaseClass[Inner]
//  implicit val tst1                = TSType.getOrGenerate[Seq[Inner]]
//  implicit val tst2: TSType[Outer] = TSType.fromCaseClass[Outer]
//  val a                    = TSType.getOrGenerate[Inner]
//  given TSType[Inner]      = a
//  val b                    = TSType.getOrGenerate[Seq[Inner]]
//  given TSType[Seq[Inner]] = b
//  val x                    = TSType.getOrGenerate[Outer]

  given TSType[Outer] = TSType.getOrGenerate[Outer]
}
