package com.scalatsi

trait ScalaVersionSpecificTypes {

  given iterableTsType[T: TSType, Coll <: Iterable[T]]: TSType[Coll] with
    def get: TypescriptType = summon[TSType[T]].get.array

//  given seqTsType[T: TSType]: TSType[Seq[T]] with
//    def get: TypescriptType = summon[TSType[T]].get.array

  //implicit def tsTraversable[E, F[_]](implicit e: TSType[E], ev: F[E] <:< Iterable[E]): TSType[F[E]] = TSType(e.get.array)

//  given setTsType[T: TSType]: TSType[Set[T]] with
//    def get: TypescriptType = summon[TSType[T]].get.array

  given javaCollectionTsType[T: TSType]: TSType[java.util.Collection[T]] with
    def get: TypescriptType = summon[TSType[T]].get.array
}
