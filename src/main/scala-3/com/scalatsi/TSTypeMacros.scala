package com.scalatsi

import scala.deriving.Mirror
import scala.compiletime.summonInline
import scala.compiletime.erasedValue

trait TSTypeMacros {
  
  /** Derive a TSType[T] for all types supporting automatic derivation in Scala 3, like case classes and sealed traits. */
  inline def derived[T](using m : Mirror.Of[T]): TSType[T] = {
    val elemInstances = summonAll[m.MirroredElemTypes]
    inline m match {
      case s: Mirror.SumOf[T] => eqSum(s, elemInstances)
      case p: Mirror.ProductOf[T] => eqProduct(p, elemInstances)
    }
  }

  private inline def summonAll[T <: Tuple]: List[TSType[_]] = {
    inline erasedValue[T] match {
      case _: EmptyTuple => Nil
      case _: (t *: ts) => summonInline[TSType[t]] :: summonAll[ts]
    }
  }

  /** Get a TSType for a Product type (case class). */
  def tsTypeProduct[T](p: Mirror.ProductOf[T], elems: => List[TSType[_]]): TSType[T] =
    new TSType[T]:
      def get(): TypescriptType =
        iterator(x).zip(iterator(y)).zip(elems.iterator).forall {
          case ((x, y), elem) => check(elem)(x, y)
        }



  /** Get an implicit `TSType[T]` or generate a default one
    *
    * By default
    * Case class will use [[fromCaseClass]]
    * Sealed traits/classes will use [[fromSealed]]
    */
  inline def getOrGenerate[T]: TSType[T] = Macros.getImplicitMappingOrGenerateDefault[T]

  /** Generate a typescript interface for a case class */
  inline def fromCaseClass[T]: TSIType[T] = Macros.generateInterfaceFromCaseClass[T]

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
  inline def fromSealed[T]: TSNamedType[T] = Macros.generateUnionFromSealedTrait[T]
}

trait TSNamedTypeMacros {

  /** Get an implicit `TSNamedType[T]` or generate a default one
    *
    * @see [[TSType.getOrGenerate]]
    */
  inline def getOrGenerate[T]: TSNamedType[T] = Macros.getImplicitNamedMappingOrGenerateDefault[T]
}

trait TSITypeMacros {

  /** Get an implicit `TSIType[T]` or generate a default one
    *
    * @see [[TSType.getOrGenerate]]
    */
  inline def getOrGenerate[T]: TSIType[T] = Macros.getImplicitInterfaceMappingOrGenerateDefault[T]
}
