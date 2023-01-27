package com.scalatsi

trait TSTypeMacros {

  /** Get an implicit `TSType[T]` or generate a default one
    *
    * By default
    * Case class will use [[fromCaseClass]]
    * Sealed traits/classes will use [[fromSealed]]
    */
  def getOrGenerate[T]: TSType[T] = macro Macros.getImplicitMappingOrGenerateDefault[T, TSType]

  /** Generate a typescript interface for a case class */
  def fromCaseClass[T]: TSIType[T] = macro Macros.generateInterfaceFromCaseClass[T, TSType]

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
  def fromSealed[T]: TSNamedType[T] = macro Macros.generateUnionFromSealedTrait[T, TSType]
}

trait TSNamedTypeMacros {

  /** Get an implicit `TSNamedType[T]` or generate a default one
    *
    * @see [[TSType.getOrGenerate]]
    */
  def getOrGenerate[T]: TSNamedType[T] = macro Macros.getImplicitMappingOrGenerateDefault[T, TSNamedType]
}

trait TSITypeMacros {

  /** Get an implicit `TSIType[T]` or generate a default one
    *
    * @see [[TSType.getOrGenerate]]
    */
  def getOrGenerate[T]: TSIType[T] = macro Macros.getImplicitInterfaceMappingOrGenerateDefault[T, TSType, TSIType]
}
