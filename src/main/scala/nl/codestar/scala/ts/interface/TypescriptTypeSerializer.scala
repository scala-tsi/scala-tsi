package nl.codestar.scala.ts.interface

import TypescriptType._

object TypescriptTypeSerializer {
  def serialize(tp: TypescriptType): String = tp match {
    case TSAlias(name, _) => name
    case TSAny => "any"
    case TSArray(elements) => serialize(elements) + "[]"
    case TSBoolean => "boolean"
    case TSInterface(name, _) => name
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

  def emit[T](implicit tsType: TSType[T]): String = emits(tsType)
  def emits(types: TSType[_]*): String =
    types.toSet
      .flatMap(
        (t: TSType[_]) =>
          t.get match {
            case interface: TSInterface => discoverNested(interface)
            case plain => Set(plain)
        }
      )
      .toSeq
      .collect({
        case alias: TSAlias => emitAlias(alias)
        case interface: TSInterface => emitInterface(interface)
      })
      .mkString("\n")

  private def emitInterface(interface: TSInterface): String = interface match {
    case TSInterface(name, members) => {
      val mbs = members.map({
        case TSInterface.Member(memberName, tp, true) =>
          s"  $memberName: ${serialize(tp)}"
        case TSInterface.Member(memberName, tp, false) =>
          s"  $memberName?: ${serialize(tp)}"
      })

      s"""
         |interface $name {
         |${mbs.mkString("\n")}
         |}
       """.stripMargin
    }
  }

  private def emitAlias(alias: TSAlias): String = alias match {
    case TSAlias(name, underlying) => s"type $name = ${serialize(underlying)}"
  }

  // TODO: Memoize or something, tail rec etc
  private def discoverNested(tp: TypescriptType): Set[TypescriptType] =
    tp match {
      case TSInterface(_, members) =>
        members.map(_.tp).toSet.flatMap(discoverNested) + tp
      case TSAlias(_, underlying) => discoverNested(underlying) + tp
      case TSTuple(members) => members.toSet.flatMap(discoverNested)
      case other => Set(other)
    }
}
