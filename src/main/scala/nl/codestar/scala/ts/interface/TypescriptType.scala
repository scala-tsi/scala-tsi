package nl.codestar.scala.ts.interface

import scala.collection.immutable.ListMap

sealed trait TypescriptType

object TypescriptType {

  /** A marker trait for a TS type that has a name */
  sealed trait TypescriptNamedType extends TypescriptType {
    def name: String
  }
  object TypescriptNamedType {
    def unapply(namedType: TypescriptNamedType): Option[String] =
      Some(namedType.name)
  }

  /** A marker trait for a TS type that can contain nested types */
  sealed trait TypescriptAggregateType extends TypescriptType {
    def nested: Set[TypescriptType]
  }
  object TypescriptAggregateType {
    def unapply(
        aggregateType: TypescriptAggregateType): Option[Set[TypescriptType]] =
      Some(aggregateType.nested)
  }

  case class TSAlias(name: String, underlying: TypescriptType)
      extends TypescriptNamedType
  case object TSAny extends TypescriptType
  case class TSArray(elementType: TypescriptType)
      extends TypescriptAggregateType { def nested = Set(elementType) }
  case object TSBoolean extends TypescriptType
  // TODO: TS Enum
  // case object TSEnum extends TypescriptBasicType
  /** Represents Typescript indexed interfaces
    * interface name { [indexName:indexType]: valueType}
    * @param indexType index type, TSNumber or TSString
    **/
  case class TSIndexedInterface(name: String,
                                indexName: String,
                                indexType: TypescriptType,
                                valueType: TypescriptType)
      extends TypescriptNamedType
      with TypescriptAggregateType {
    require(
      indexType == TSString || indexType == TSNumber,
      s"TypeScript indexed interface $name can only have index type string or number, not $indexType")
    def nested = Set(indexType, valueType)
  }
  case class TSInterface(name: String,
                         members: ListMap[String, TypescriptType])
      extends TypescriptNamedType
      with TypescriptAggregateType {
    def nested = members.values.toSet
  }
  case class TSIntersection(of: Seq[TypescriptType])
      extends TypescriptAggregateType { def nested = of.toSet }
  object TSIntersection {
    def of(of: TypescriptType*) = TSIntersection(of)
  }
  case object TSNever extends TypescriptType
  case object TSNull extends TypescriptType
  case object TSNumber extends TypescriptType
  case object TSString extends TypescriptType
  case class TSTuple[E](of: Seq[TypescriptType])
      extends TypescriptAggregateType { def nested = of.toSet }
  object TSTuple {
    def of(of: TypescriptType*) = TSTuple(of)
  }
  case object TSUndefined extends TypescriptType
  case class TSUnion(of: Seq[TypescriptType]) extends TypescriptAggregateType {
    def nested = of.toSet
  }
  object TSUnion {
    def of(of: TypescriptType*) = TSUnion(of)
  }
  case object TSVoid extends TypescriptType
}
