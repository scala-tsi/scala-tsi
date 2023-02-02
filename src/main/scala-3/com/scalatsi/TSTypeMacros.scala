package com.scalatsi

import TypescriptType.*

import scala.collection.immutable.ListMap

import scala.deriving.Mirror
import scala.compiletime.*

trait TSTypeMacros {
  inline given [T: Mirror.Of]: TSType[T] = derived[T]

  /** Derive a TSType[T] for all types supporting automatic derivation in Scala 3, like case classes and sealed traits. */
  inline def derived[T](using m: Mirror.Of[T]): TSType[T] = {
    val elemInstances = summonAll[m.MirroredElemTypes]
    inline m match {
      case s: Mirror.SumOf[T]     => tsTypeSum(s, elemInstances)
      case p: Mirror.ProductOf[T] => tsTypeProduct(p, elemInstances)
    }
  }

  private inline def summonAll[T <: Tuple]: List[TSType[_]] = {
    inline erasedValue[T] match {
      case _: EmptyTuple => Nil
      case _: (t *: ts)  => summonInline[TSType[t]] :: summonAll[ts]
    }
  }

  /** Get a TSType for a Product type (case class). */
  private inline def tsTypeProduct[T](p: Mirror.ProductOf[T], elems: List[TSType[_]]): TSIType[T] = {
    val tsElems: Seq[(String, TypescriptType)] =
      elemNames[p.MirroredElemLabels]
        .zip(elems)
        .map((name, tstype) => (name, tstype.get))

    val interface = TSInterface(tsName(p), ListMap(tsElems*))
    TSIType[T](interface)
  }

  private inline def tsTypeSum[T](s: Mirror.SumOf[T], elems: List[TSType[_]]): TSNamedType[T] = {
    val aliasFor: TypescriptType = elems match {
      case Nil          => TSNever
      case List(single) => TypescriptType.nameOrType(single.get)
      case multiple =>
        val children: Seq[TypescriptType] =
          multiple
            .zip(elemNames[s.MirroredElemLabels])
            .map((tstype, name) => TypescriptType.nameOrType(tstype.get, discriminator = Some(name)))
        TSUnion(children)
    }
    TSNamedType(TSAlias(tsName(s), aliasFor))
  }

  private inline def tsName(p: Mirror): String =
    (if (p.isInstanceOf[Mirror.Product]) "I" else "") +
      constValue[p.MirroredLabel]

  private inline def elemNames[T <: Tuple]: List[String] =
    constValueTuple[T].productIterator.toList.asInstanceOf[List[String]]

  /** Get an implicit `TSType[T]` or generate a default one
    *
    * By default
    * Case class will use [[fromCaseClass]]
    * Sealed traits/classes will use [[fromSealed]]
    */
  inline def getOrGenerate[T]: TSType[T] = Macros.getImplicitMappingOrGenerateDefault[T]

  /** Generate a typescript interface for a case class */
  inline def fromCaseClass[T: Mirror.ProductOf]: TSIType[T] = derived[T].asInstanceOf[TSIType[T]]

  /** Generate a Typescript discriminated union from a scala sealed trait
    *
    * @example
    * ```
    * sealed trait AorB
    * case class A(foo: Int) extends AorB
    * case class B(bar: Int) extends AorB
    *
    * implicit val tsAorB = TSType.fromSealed[AorB]
    * ```
    *
    * wil produce
    *
    * `type AorB = A | B`
    * @see [Typescript docs on Discriminated Unions](https://www.typescriptlang.org/docs/handbook/unions-and-intersections.html#discriminating-unions)
    */
  inline def fromSealed[T: Mirror.SumOf]: TSNamedType[T] = derived[T].asInstanceOf[TSNamedType[T]]
}

trait TSNamedTypeMacros {
  inline given [T: Mirror.Of]: TSNamedType[T]      = derived[T]
  inline def derived[T: Mirror.Of]: TSNamedType[T] = TSType.derived[T].asInstanceOf[TSNamedType[T]]

  /** Get an implicit `TSNamedType[T]` or generate a default one
    *
    * @see [[TSType.getOrGenerate]]
    */
  inline def getOrGenerate[T]: TSNamedType[T] = Macros.getImplicitNamedMappingOrGenerateDefault[T]
}

trait TSITypeMacros {
  inline given [T: Mirror.ProductOf]: TSIType[T]      = derived[T]
  inline def derived[T: Mirror.ProductOf]: TSIType[T] = TSType.fromCaseClass[T]

  /** Get an implicit `TSIType[T]` or generate a default one
    *
    * @see [[TSType.getOrGenerate]]
    */
  inline def getOrGenerate[T]: TSIType[T] = Macros.getImplicitInterfaceMappingOrGenerateDefault[T]
}
