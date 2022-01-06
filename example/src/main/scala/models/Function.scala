package models

import com.scalatsi._
import com.scalatsi.TypescriptType._

import java.util.UUID

case class Greeter(anonymousFunction: (UUID) => Int, namedFunction: GreetFunction, voidFunction: () => Unit)

trait GreetFunction extends ((String, Int) => String) {
  // TODO: Can we extract the parameter names from the function to export?
  override def apply(name: String, age: Int): String
}
object GreetFunction {
  // TODO: Infer this automatically
  implicit val tsType: TSNamedType[GreetFunction] = TSNamedType(
    TSFunctionNamed("GreetFunction", TSType.get[(String, Int) => String].get.asInstanceOf[TSFunction])
  )
}
