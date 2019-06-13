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
case class TSNamespace private(parts: IndexedSeq[TSIdentifier]) {

  override def toString: String = parts.mkString(".")

  /** The namespace without a common prefix
    * @example "foo.bar.baz.A" and "foo.bar." will give "baz" */
  def withoutCommonPrefix(other: TSNamespace): TSNamespace = TSNamespace(parts.dropCommonPrefix(other.parts))
}

object TSNamespace {
  def apply(ids: IndexedSeq[TSIdentifier]): TSNamespace = new TSNamespace(ids)
  def apply(pck: Package): TSNamespace = apply(pck.getName)
  def apply(namespace: String): TSNamespace = TSNamespace(namespace.split('.').map(TSIdentifier.apply))
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
