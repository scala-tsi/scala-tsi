package nl.codestar.scala.ts.interface

sealed trait TypescriptType
object TypescriptType {
  case class TSAlias(name: String, underlying: TypescriptType)
      extends TypescriptType
  case object TSAny extends TypescriptType
  case class TSArray(elements: TypescriptType) extends TypescriptType
  case object TSBoolean extends TypescriptType
  // TODO: TS Enum
  // case object TSEnum extends TypescriptBasicType
  case class TSInterface(name: String, members: Seq[TSInterface.Member])
      extends TypescriptType
  case object TSInterface {
    // TODO: Still generates a Member.apply(String,TypescriptType,Boolean) in scala 2.11
    case class Member private (name: String,
                               tp: TypescriptType,
                               required: Boolean)
    object Member {
      def apply(name: String, tp: TypescriptType): Member = tp match {
        case TSUnion(union) =>
          Member.apply(name,
                       TSUnion(union.filter(_ != TSUndefined)),
                       required = !union.contains(TSUndefined))
        case t => Member.apply(name, t, required = true)
      }

    }
  }
  case object TSNever extends TypescriptType
  case object TSNull extends TypescriptType
  case object TSNumber extends TypescriptType
  case object TSString extends TypescriptType
  case class TSTuple[E](members: Seq[TypescriptType]) extends TypescriptType
  object TSTuple {
    def of(of: TypescriptType*) = TSTuple(of)
  }
  case object TSUndefined extends TypescriptType
  case class TSUnion(of: Seq[TypescriptType]) extends TypescriptType
  object TSUnion {
    def of(of: TypescriptType*) = TSUnion(of)
  }
  case object TSVoid extends TypescriptType
}
