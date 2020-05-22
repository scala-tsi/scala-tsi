package models

import nl.codestar.scalatsi.TSType
import nl.codestar.scalatsi.DefaultTSTypes._
import nl.codestar.scalatsi.dsl._

/*************************************
  * Examples from the project README
  ***********************************/

case class Person(
  name: String,
  email: Email,
  age: Option[Int],
  // for privacy reasons, we do not put this social security number in the JSON
  ssn: Option[Int],
  job: Job
)
// This type will get erased when serializing to JSON, only the string remains
case class Email(address: String)

case class Job(tasks: Seq[String], boss: String)

object ReadmeTSTypes {
  implicit val tsEmail  = TSType.sameAs[Email, String]
  implicit val tsPerson = TSType.fromCaseClass[Person] - "ssn"
}
