package nl.codestar.scalatsi

import java.util.regex.Pattern

import scala.reflect.ClassTag
import scala.util.Try

import scala.collection.compat.immutable.ArraySeq

/** This type is used as a marker that a type with this name exists and is either already defined or externally defined
  * @note name takes from [Typescript specification](https://github.com/Microsoft/TypeScript/blob/master/doc/spec.md#3.8.2)
  * */
case class TSRef private (name: TSIdentifier, namespace: TSNamespace) {
  override def toString: String = {
    val nss = namespace.toString
    s"$nss${if (nss != "") "." else ""}${name.id}"
  }

  def withName(name: String): TSRef           = this.copy(name = TSIdentifier(name))
  def withNamespace(namespace: String): TSRef = this.copy(namespace = TSNamespace(namespace))
}
object TSRef {
  def apply(name: TSIdentifier, namespace: TSNamespace): TSRef = new TSRef(name, namespace)
  def apply(fqn: String): TSRef = {
    val namespaceWithId = TSNamespace(fqn)
    TSRef(namespaceWithId.parts.head, TSNamespace(namespaceWithId.parts.tail))
  }
  def apply[T: ClassTag]: TSRef = TSRef(TSIdentifier[T], TSNamespace[T])

  def unapply(arg: TSRef): Option[(String, Seq[String])] = Some((arg.name.id, arg.namespace.parts.map(_.id)))
}

/** A valid typescript identifier
  * @see https://github.com/Microsoft/TypeScript/blob/master/doc/spec.md#382-type-references */
case class TSIdentifier private (id: String) {
  import TSIdentifier._
  require(isValidTSName(id))
  override def toString: String = id
}

object TSIdentifier {
  /** Default identifier to use as a stand-in on the rare ocasion that a JVM name is not a valid Typescript name */
  final val INVALID = TSIdentifier("INVALID")

  /** Transform a string to a typescript identifier
    * @throws IllegalArgumentException if the name is not a valid typescript identifier */
  def apply(name: String): TSIdentifier = new TSIdentifier(name)

  /** Transform a type into a typescript identifier */
  def apply[T](implicit ct: ClassTag[T]): TSIdentifier = TSIdentifier.idOrInvalid(ct.getClass.getSimpleName)

  def unapply(arg: TSIdentifier): Option[String] = Some(arg.id)

  def idOrInvalid(name: String): TSIdentifier = Try(apply(name)).getOrElse(INVALID)


  def isValidTSName(name: String): Boolean =
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
case class TSNamespace(parts: IndexedSeq[TSIdentifier]) {

  override def toString: String = parts.mkString(".")

  /** The namespace without a common prefix
    * @example "foo.bar.baz.A" and "foo.bar." will give "baz" */
  def withoutCommonPrefix(other: TSNamespace): TSNamespace = TSNamespace(parts.dropCommonPrefix(other.parts))
}

object TSNamespace {
  def apply(parts: String*): TSNamespace              = new TSNamespace(parts.map(TSIdentifier.idOrInvalid).toIndexedSeq)
  def apply(namespace: String): TSNamespace           = TSNamespace(ArraySeq.unsafeWrapArray(namespace.split('.')).map(TSIdentifier.idOrInvalid))
  def apply(pck: Package): TSNamespace                = apply(pck.getName)
  def apply[T](implicit ct: ClassTag[T]): TSNamespace = apply(ct.getClass.getPackage)

  def parse(namespace: String): Try[TSNamespace] = Try(apply(namespace))

  def unapplySeq(arg: TSNamespace): Option[Seq[String]] = Some(arg.parts.map(_.id))
}
