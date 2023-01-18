package com.scalatsi
import scala.annotation.unused

trait ScalaVersionSpecificTypes extends LowPriorityCollectionTSType {}

trait LowPriorityCollectionTSType {

  /** Provides a TSType for any scala collection of E to a typescript array of E */
  implicit def tsTraversable[E, F[_]](implicit e: TSType[E], @unused ev: F[E] <:< Iterable[E]): TSType[F[E]] = TSType(e.get.array)

  // All java collection types implement Collection and are almost always translated to javascript arrays
  implicit def tsJavaCollection[E, F[_]](implicit
      e: TSType[E],
      @unused ev: F[E] <:< java.util.Collection[E]
  ): TSType[F[E]] = TSType(e.get.array)
}
