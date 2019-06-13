package nl.codestar.scalatsi

import java.util.regex.Pattern

import scala.reflect.ClassTag

/** A typescript identifier
  * @see https://github.com/Microsoft/TypeScript/blob/master/doc/spec.md#382-type-references */
case class TSIdentifier private (id: String) {
  import TSIdentifier._
  require(isValidTSName(id))
  override def toString: String = id
}

object TSIdentifier {
  def apply(id: String)                  = new TSIdentifier(id)
  def apply[T](implicit ct: ClassTag[T]) = TSIdentifier(ct.getClass.getSimpleName)

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

/** A typescript namespace
  * @see https://github.com/Microsoft/TypeScript/blob/master/doc/spec.md#3.8.2 */
case class TSNamespace private (parts: IndexedSeq[TSIdentifier]) {

  override def toString: String = parts.mkString(".")

  /** The namespace without a common prefix
    * @example "foo.bar.baz.A" and "foo.bar." will give "baz" */
  def withoutCommonPrefix(other: TSNamespace): TSNamespace = TSNamespace(parts.dropCommonPrefix(other.parts))
}

object TSNamespace {
  def apply(parts: IndexedSeq[TSIdentifier]): TSNamespace = new TSNamespace(parts)
  def apply(namespace: String): TSNamespace               = TSNamespace(namespace.split('.').map(TSIdentifier(_)))
  def apply(pck: Package): TSNamespace                    = apply(pck.getName)
  def apply[T](implicit ct: ClassTag[T])                  = apply(ct.getClass.getPackage)
}
