package nl.codestar.scala.ts.interface

import TypescriptType._

object TypescriptTypeSerializer {
  // TODO: Optimize? Memoize? Tailrec?
  def serialize(tp: TypescriptType): String = tp match {
    case TypescriptNamedType(name) => name
    case TSAny => "any"
    case TSArray(elements) => serialize(elements) + "[]"
    case TSBoolean => "boolean"
    case TSIntersection(Seq()) => serialize(TSNever)
    case TSIntersection(Seq(e)) => serialize(e)
    case TSIntersection(of) => s"${of.map(serialize) mkString " | "}"
    case TSNever => "never"
    case TSNull => "null"
    case TSNumber => "number"
    case TSTuple(members) => s"[${members.map(serialize) mkString ", "}]"
    case TSString => "string"
    case TSUndefined => "undefined"
    case TSUnion(Seq()) => serialize(TSNever)
    case TSUnion(Seq(e)) => serialize(e)
    case TSUnion(of) => s"(${of.map(serialize) mkString " | "})"
    case TSVoid => "void"
  }

  def emit[T](implicit tsType: TSNamedType[T]): String = emits(tsType)
  def emits(types: TSNamedType[_]*): String =
    emitNamedTypes(types.map(_.get): _*)

  private def emitNamedTypes(types: TypescriptNamedType*): String =
    types.toSet
      .flatMap(discoverNestedNames)
      .toSeq
      .map(emitNamed)
      .mkString("\n")

  private object TSInterfaceEntry {
    def unapply(
        typescriptType: TypescriptType): Option[(TypescriptType, Boolean)] =
      typescriptType match {
        case TSUnion(members) if members.contains(TSUndefined) =>
          Some((TSUnion(members.filter(_ != TSUndefined)), false))
        case other => Some((other, true))
      }
  }

  private def emitNamed(named: TypescriptNamedType): String = named match {
    case TSAlias(name, underlying) =>
      s"type $name = ${serialize(underlying)}"
    case TSEnum(name, const, entries) =>
      val mbs = entries.map({
        case (entryName, Some(i)) => s"  $entryName = $i"
        case (entryName, None) => s"  $entryName"
      })
      s"""${if (const) "const " else ""}enum $name {
         |${mbs.mkString(",\n")}
         |}
       """.stripMargin
    case TSIndexedInterface(name, indexName, indexType, valueType) =>
      s"""interface $name {
         |  [$indexName: $indexType]: $valueType
         |}
       """.stripMargin
    case TSInterface(name, members) =>
      def symbol(required: Boolean) = if (required) ":" else "?:"

      val mbs = members.map({
        case (memberName, TSInterfaceEntry(tp, required)) =>
          s"  $memberName${symbol(required)} ${serialize(tp)}"
      })

      s"""
         |interface $name {
         |${mbs.mkString("\n")}
         |}
       """.stripMargin
  }

  // TODO: Optimize, Memoize or something, tail rec etc
  private def discoverNestedNames(
      tp: TypescriptType): Set[TypescriptNamedType] = {
    val me: Set[TypescriptNamedType] = tp match {
      case named: TypescriptNamedType => Set(named)
      case _ => Set()
    }
    tp match {
      case TypescriptAggregateType(nested) =>
        nested.flatMap(discoverNestedNames) ++ me
      case _ => me
    }
  }
}
