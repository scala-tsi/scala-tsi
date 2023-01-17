package com.scalatsi

import java.util.regex.Pattern

import scala.collection.immutable.ListMap
import TypescriptType._

sealed trait TypescriptType {
  def |(tt: TypescriptType): TSUnion = TSUnion.of(this, tt).flatten
  def array: TSArray                 = TSArray(this)
}

object TypescriptType {
  private[scalatsi] def fromString(tpe: String): TypescriptType =
    tpe match {
      case "any"       => TSAny
      case "boolean"   => TSBoolean
      case "never"     => TSNever
      case "null"      => TSNull
      case "number"    => TSNumber
      case "string"    => TSString
      case "undefined" => TSUndefined
      case "void"      => TSVoid
      case "object"    => TSObject
      case _           => TSTypeReference(tpe)
    }

  /** Get a reference to a named type, or the type itself if it is unnamed or built-in */
  def nameOrType(tpe: TypescriptType, discriminator: Option[String] = None): TypescriptType = tpe match {
    case named: TypescriptNamedType => named.asReference(discriminator)
    case anonymous                  => anonymous
  }

  /** A marker trait for a TS type that has a name */
  sealed trait TypescriptNamedType extends TypescriptType {
    def name: String
    require(isValidTSName(name), s"Not a valid TypeScript identifier: $name")

    /** Whether this type should be referenced with a type query [`typeof name`](https://github.com/microsoft/TypeScript/blob/main/doc/spec-ARCHIVED.md#3.8.10) */
    def useTypeQuery: Boolean = false

    def asReference(discriminator: Option[String] = None): TSTypeReference = TSTypeReference(name, Some(this), discriminator, useTypeQuery)

    def withName(newName: String): TypescriptNamedType
  }
  object TypescriptNamedType {
    implicit val ordering: Ordering[TypescriptNamedType] = Ordering.by[TypescriptNamedType, String](_.name)
  }

  /** A marker trait for a TS type that can contain nested types */
  sealed trait TypescriptAggregateType extends TypescriptType {
    def nested: Set[TypescriptType]
  }
  object TypescriptAggregateType {
    def unapply(aggregateType: TypescriptAggregateType): Option[Set[TypescriptType]] =
      Some(aggregateType.nested)
  }

  /** `type name = underlying` */
  case class TSAlias(name: String, underlying: TypescriptType) extends TypescriptNamedType with TypescriptAggregateType {
    override def nested: Set[TypescriptType]        = Set(underlying)
    override def withName(newName: String): TSAlias = copy(name = newName)
  }

  case object TSAny                               extends TypescriptType
  case class TSArray(elementType: TypescriptType) extends TypescriptAggregateType { def nested: Set[TypescriptType] = Set(elementType) }
  case object TSBoolean                           extends TypescriptType

  sealed trait TSLiteralType[T]                 extends TypescriptType { val value: T }
  case class TSLiteralString(value: String)     extends TSLiteralType[String]
  case class TSLiteralNumber(value: BigDecimal) extends TSLiteralType[BigDecimal]
  case class TSLiteralBoolean(value: Boolean)   extends TSLiteralType[Boolean]

  case class TSEnum(name: String, const: Boolean, entries: ListMap[String, Option[TSLiteralType[?]]])
      extends TypescriptNamedType
      with TypescriptAggregateType {
    def nested: Set[TypescriptType]                = entries.values.flatMap(_.toSeq).toSet
    override def withName(newName: String): TSEnum = copy(name = newName)
  }
  object TSEnum {
    def of(name: String, entries: String*): TSEnum =
      TSEnum(name, const = false, ListMap(entries.map(e => e -> None): _*))
    def numeric(name: String, entries: (String, Int)*): TSEnum =
      TSEnum(name, const = false, ListMap(entries.map({ case (e, value) => e -> Some(TSLiteralNumber(value)) }): _*))
    def string(name: String, entries: (String, String)*): TSEnum =
      TSEnum(name, const = false, ListMap(entries.map({ case (e, value) => e -> Some(TSLiteralString(value)) }): _*))
  }

  /** Anonymous Typescript function */
  // TODO: Add support for generics?
  // TODO: Add support for type guards
  case class TSFunction(arguments: ListMap[String, TypescriptType] = ListMap(), returnType: TypescriptType = TSVoid)
      extends TypescriptType
      with TypescriptAggregateType {
    override def nested: Set[TypescriptType] = arguments.values.toSet + returnType
  }

  /** A named Typescript function
    * function name(arg1: type1, arg2: type2): returnType;
    */
  case class TSFunctionNamed(name: String, signature: TSFunction)
      extends TypescriptType
      with TypescriptAggregateType
      with TypescriptNamedType {
    override def useTypeQuery: Boolean                      = true
    override def nested: Set[TypescriptType]                = signature.nested
    override def withName(newName: String): TSFunctionNamed = copy(name = newName)
  }

  /** Typescript anonymous indexed interfaces
    * { [indexName:indexType]: valueType }
    * @param indexType index type, TSNumber or TSString
    */
  case class TSIndexedInterface(indexName: String = "key", indexType: TypescriptType, valueType: TypescriptType)
      extends TypescriptAggregateType {
    require(
      indexType == TSString || indexType == TSNumber,
      s"TypeScript indexed interface can only have index type string or number, not $indexType"
    )
    def nested: Set[TypescriptType] = Set(indexType, valueType)
  }
  case class TSInterfaceIndexed(name: String, indexName: String = "key", indexType: TypescriptType, valueType: TypescriptType)
      extends TypescriptNamedType
      with TypescriptAggregateType {
    require(
      indexType == TSString || indexType == TSNumber,
      s"TypeScript indexed interface $name can only have index type string or number, not $indexType"
    )
    def nested: Set[TypescriptType]                            = Set(indexType, valueType)
    override def withName(newName: String): TSInterfaceIndexed = copy(name = newName)
  }

  case class TSInterface(name: String, members: ListMap[String, TypescriptType]) extends TypescriptNamedType with TypescriptAggregateType {
    def nested: Set[TypescriptType] = members.values.toSet

    override def withName(newName: String): TSInterface = copy(name = newName)
  }

  case class TSIntersection(of: Seq[TypescriptType]) extends TypescriptAggregateType { def nested: Set[TypescriptType] = of.toSet }
  object TSIntersection {
    def of(of: TypescriptType*): TSIntersection = TSIntersection(of)
  }
  case object TSNever  extends TypescriptType
  case object TSNull   extends TypescriptType
  case object TSNumber extends TypescriptType
  case object TSObject extends TypescriptType
  case object TSString extends TypescriptType

  /** Typescript tuple: `[0.type, 1.type, ... n.type]` */
  case class TSTuple(of: Seq[TypescriptType]) extends TypescriptAggregateType {
    def nested: Set[TypescriptType] = of.toSet
  }
  object TSTuple {
    def of(of: TypescriptType*): TSTuple = TSTuple(of)
  }

  /** This type is used as a marker that a type with this name exists and is either already defined or externally defined.
    * Not a real Typescript type
    * @note name takes from [Typescript specification](https://github.com/microsoft/TypeScript/blob/main/doc/spec-ARCHIVED.md#3.8.2)
    * @param impl The implementation of the type if it is known, so that the nested types can be outputted even if not directly referenced
    * @param discriminator the discriminator value for the type if this type is part of a discriminated union
    * @param useTypeQuery Whether this is a type query [`typeof name`](https://github.com/microsoft/TypeScript/blob/main/doc/spec-ARCHIVED.md#3.8.10)
    */
  case class TSTypeReference(
      name: String,
      impl: Option[TypescriptType] = None,
      discriminator: Option[String] = None,
      override val useTypeQuery: Boolean = false
  ) extends TypescriptNamedType
      with TypescriptAggregateType {
    override def asReference(discriminator: Option[String] = None): TSTypeReference =
      if (discriminator == this.discriminator) this else copy(discriminator = discriminator)
    override def nested: Set[TypescriptType]                = impl.toSet
    override def withName(newName: String): TSTypeReference = copy(name = newName)
  }

  case object TSUndefined                     extends TypescriptType
  case object TSUnknown                       extends TypescriptType
  case class TSUnion(of: Seq[TypescriptType]) extends TypescriptAggregateType {

    /** Recursively flatten this union */
    def flatten: TSUnion =
      if (of.exists(_.isInstanceOf[TSUnion]))
        TSUnion(of.flatMap({
          case nested: TSUnion => nested.flatten.of
          case other           => Seq(other)
        }))
      else this
    def nested: Set[TypescriptType] = of.toSet
  }
  object TSUnion {
    def of(of: TypescriptType*): TSUnion = TSUnion(of)
  }
  case object TSVoid extends TypescriptType

  private val tsIdentifierPattern = Pattern.compile("[_$\\p{L}\\p{Nl}][_$\\p{L}\\p{Nl}\\p{Nd}\\{Mn}\\{Mc}\\{Pc}]*")
  private[scalatsi] def isValidTSName(name: String): Boolean =
    tsIdentifierPattern.matcher(name).matches() && !reservedKeywords.contains(name)

  final private[scalatsi] val reservedKeywords: Set[String] = Set(
    "break",
    "case",
    "catch",
    "class",
    "const",
    "continue",
    "debugger",
    "default",
    "delete",
    "do",
    "else",
    "enum",
    "export",
    "extends",
    "false",
    "finally",
    "for",
    "function",
    "if",
    "import",
    "in",
    "instanceof",
    "new",
    "null",
    "return",
    "super",
    "switch",
    "this",
    "throw",
    "true",
    "try",
    "typeof",
    "var",
    "void",
    "while",
    "with",
    // Strict mode
    "as",
    "implements",
    "interface",
    "let",
    "package",
    "private",
    "protected",
    "public",
    "static",
    "yield"
  )
}
