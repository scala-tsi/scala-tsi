package models;

import com.scalatsi._

case class ListObj(a: String, b: String)

case class ParentObj(
    coolList: Seq[ListObj],
    coolEnum: MyEnum.Value
)

// TODO #176: Automatically generate these types and remove these custom definitions
object TSTypes {
  implicit val listObj: TSIType[ListObj]         = TSType.fromCaseClass[ListObj]
  implicit val myEnumValue: TSType[MyEnum.Value] = TSType.sameAs[MyEnum.Value, MyEnum.type]
}
