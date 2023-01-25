package com.scalatsi

trait TSTypeMacros {

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
