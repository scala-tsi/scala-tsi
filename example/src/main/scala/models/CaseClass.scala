package models

case class MyCaseClass(nested: Person, bool: Boolean)

case class GenericCaseClass(
    optional: Option[String],
    emails: Seq[Email],
    mapping: Map[String, Person]
)


