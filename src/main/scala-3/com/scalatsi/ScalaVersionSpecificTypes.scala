package com.scalatsi

trait ScalaVersionSpecificTypes {
  given seqTsType[T](using TSType[T]): TSType[Seq[T]] with
    def get: TypescriptType = summon[TSType[T]].get.array

  given setTsType[T](using TSType[T]): TSType[Set[T]] with
    def get: TypescriptType = summon[TSType[T]].get.array

  given javaCollectionTsType[T](using TSType[T]): TSType[java.util.Collection[T]] with
    def get: TypescriptType = summon[TSType[T]].get.array
}
