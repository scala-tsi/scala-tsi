package nl.codestar.scalatsi

import java.util.regex.Pattern

/** A typescript identifier
  * @see https://github.com/Microsoft/TypeScript/blob/master/doc/spec.md#382-type-references */
case class TSIdentifier(id: String) {
  import TSIdentifier._
  require(isValidTSName(id))
  override def toString: String = id
}

/** A typescript namespace
  * @see https://github.com/Microsoft/TypeScript/blob/master/doc/spec.md#3.8.2 */
case class TSNamespace(ids: IndexedSeq[TSIdentifier]) extends AnyVal {

  override def toString: String = ids.mkString(".")

  /** Strip the common prefix of two namespaces
    * @example "foo.bar.baz.A" */
  def commonPrefix(other: TSNamespace): TSNamespace = {
    var i = 0
    // set i to the last common prefix
    while (i < other.ids.length && ids(i) == other.ids(i)) { i += 1 }
    TSNamespace(ids.drop(i))
  }
}

object TSNamespace {
  def apply(ids: IndexedSeq[TSIdentifier]): TSNamespace = new TSNamespace(ids)
}

object TSIdentifier {
  final def isValidTSName(name: String): Boolean =
    tsIdentifierPattern.matcher(name).matches() && !reservedKeywords.contains(name)

  private val tsIdentifierPattern = Pattern.compile("[_$\\p{L}\\p{Nl}][_$\\p{L}\\p{Nl}\\p{Nd}\\{Mn}\\{Mc}\\{Pc}]*")

  private val reservedKeywords: Set[String] = Set(
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
