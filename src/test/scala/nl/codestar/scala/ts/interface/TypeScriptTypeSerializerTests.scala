package nl.codestar.scala.ts.interface

import java.lang.reflect.Field
import nl.codestar.scala.ts.interface.TypescriptType.TSInterface
import org.scalatest.{FlatSpec, Matchers}
import scala.collection.immutable.ListMap
import scala.reflect.ClassTag
import scala.meta._
import scala.reflect.macros.blackbox
import scala.reflect.runtime.universe._
import scala.language.experimental.macros

class TypeScriptTypeSerializerTests extends FlatSpec with Matchers {

  "Typescript type serializer" should "be able to generate typescript for a simple interface" in {

    implicit val personTsWrites = TypeScriptWriter.writes[Person]

    val x = TypescriptTypeSerializer.mkInterface(personTsWrites.get)

    x.trim should be("""
        |interface Person {
        |  age : number
        |  name : string
        |}
      """.stripMargin.trim)
  }

  object TypeScriptWriter {

    def writes[T: TypeTag](implicit m: reflect.Manifest[T]): TSIType[T] = {
      val `type` = typeOf[T]

      TSIType.apply(
        name = m.runtimeClass.getSimpleName,
        members = `type`.members
          .filter(!_.isMethod)
          .map {
            case t if t.typeSignature <:< typeOf[String] =>
              TSInterface.TSInterfaceMember(t.name.toString, TypescriptType.TSString, true)
            case t if t.typeSignature <:< typeOf[Int] =>
              TSInterface.TSInterfaceMember(t.name.toString, TypescriptType.TSNumber, true)
          }
          .toSeq
      )

    }
  }
}

private class Macros(val c: blackbox.Context) {
  import c.universe._

  private def primaryConstructor(T: Type): MethodSymbol =
    T.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor =>
        if (!m.isPublic)
          c.error(c.enclosingPosition, s"Only classes with public primary constructor are supported. Found: $T")
        m
    }.get

  private def caseClassFieldsTypes(T: Type): ListMap[String, Type] = {
    val paramLists = primaryConstructor(T).paramLists
    val params = paramLists.head

    if (paramLists.size > 1)
      c.error(c.enclosingPosition, s"Only one parameter list classes are supported. Found: $T")

    params.foreach { p =>
      if (!p.isPublic)
        c.error(
          c.enclosingPosition,
          s"Only classes with all public constructor arguments are supported. Found: $T"
        )
    }

    ListMap(params.map { field =>
      (field.name.toTermName.decodedName.toString, field.infoIn(T))
    }: _*)
  }

  def foo[T: c.WeakTypeTag](ev: Tree): Tree = {
    val T = c.weakTypeOf[T]

    if (!isCaseClass(T))
      c.error(c.enclosingPosition, s"Expected case class, but found: $T")

    val members = caseClassFieldsTypes(T).map {
      case (key, tpe) =>
        val name = TermName(c.freshName)
        q"val $name = TSInterfaceMember($key, TypescriptType($tpe), true)"
    }

    val name = TermName(c.freshName)

    q"""implicit val $name = TSIType($name, $members)"""
  }

  protected def isCaseClass(tpe: Type) =
    tpe.typeSymbol.isClass && tpe.typeSymbol.asClass.isCaseClass
}

object Typescript {
  def writes[T] = macro Macros.foo[T]
}

case class Person(name: String, age: Int)
