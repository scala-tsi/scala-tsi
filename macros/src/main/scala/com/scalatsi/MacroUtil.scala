package com.scalatsi

import scala.reflect.macros.blackbox

/** Generic Macro utility not really related to specific scala-tsi logic */
private[scalatsi] class MacroUtil[C <: blackbox.Context](val c: C) {

  case class CircularReference(T: c.Type)

  def lookupOptionalImplicit(T: c.Type): Either[CircularReference, Option[c.Tree]] = {

    println(s"$T: ${Thread.currentThread().getStackTrace.length} $isDeepStack")

    val found =
      try {
        c.inferImplicitValue(T, silent = true)
      } catch {
        case _: Exception => c.universe.EmptyTree
      }

    Right(
      Option(found)
        .filter(_ != c.universe.EmptyTree)
    )
  }

  /** Create a type representing F[T] from a T and a F[_] */
  def properType[T: c.WeakTypeTag, F[_]](implicit tsTypeTag: c.WeakTypeTag[F[_]]) = {
    // Get the T => F[T] function
    val typeConstructor = c.weakTypeOf[F[_]].typeConstructor
    // Construct the F[T] type we need to look up
    c.universe.appliedType(typeConstructor, c.weakTypeOf[T])
  }

  /**
    * HACK: Check if we are too deep in the stack to continue
    * Circular references cause an infinite loop while searching for implicits in combination with default values
    * Multiple approaches have been tried, but no "proper" solution worked
    * Instead, we abort when the stack trace is larger than 768 entries
    * This is large enough that the nesting must be ridiciously deep before aborting (and at that point the user should define some helper implicits)
    * while not crashing with the default stack size
    */
  private def isDeepStack: Boolean =
    Thread.currentThread().getStackTrace.length >= 786
}
