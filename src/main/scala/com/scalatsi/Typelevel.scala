package com.scalatsi

import scala.annotation.implicitNotFound

/**
 * Proof that A != B
 * @see https://stackoverflow.com/questions/6909053/enforce-type-difference
 * */
@implicitNotFound(msg = "Cannot prove that ${A} is not equal to ${B}.")
sealed trait Neq[A, B]

object Neq {
  implicit def neq[A, B]: A Neq B = null
  // This pair excludes the A =:= B case
  implicit def neqAmbig1[A]: A Neq A = null
  implicit def neqAmbig2[A]: A Neq A = null
}
