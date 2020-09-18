package models

import com.scalatsi.{TSType, TSIType}

/**
 * A sealed trait will be exported as a union
 *
 * @note
 * ```
 * export type Sealed = (ISealedOption1 | ISealedOption2)
 *
 * export interface ISealedOption1 {
 *   foo: string
 * }
 * 
 * export interface ISealedOption2 {
 *   bar: number
 * }
 * ```
 */
sealed trait Sealed
case class SealedOption1(foo: String) extends Sealed
case class SealedOption2(bar: Int)    extends Sealed

/**
 * A sealed trait can have a discriminator
 *
 * @note
 * ```
 * export type DiscrimatedUnion = (IDU1 | IDU2)
 *
 * export interface IDU1 {
 *   type: "type1"
 *   field1: string
 * }
 *
 * export interface IDU2 {
 *   type: "type2"
 *   field2: number
 * }
 * ```
 */
sealed trait DiscriminatedUnion
object DiscriminatedUnion {
  case class DU1(field1: String) extends DiscriminatedUnion
  case class DU2(field2: String) extends DiscriminatedUnion

  object TSTypes {
    import com.scalatsi.dsl._
    implicit val du1Type : TSType[DU1] = TSIType.getOrGenerate[DU1] + ("type" -> "type1")
    implicit val du2Type : TSType[DU2] = TSIType.getOrGenerate[DU2] + ("type" -> "type2")
    implicit val duType : TSType[DiscriminatedUnion] = TSType.getOrGenerate[DiscriminatedUnion]
  }
}


