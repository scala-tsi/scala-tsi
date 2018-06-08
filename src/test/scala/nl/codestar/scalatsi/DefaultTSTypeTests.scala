package nl.codestar.scalatsi

import nl.codestar.scalatsi.TypescriptType._
import org.scalactic.source
import org.scalatest.words.StringVerbBlockRegistration
import org.scalatest.{Matchers, WordSpec}

class DefaultTSTypeTests extends WordSpec with Matchers with DefaultTSTypes {

  "Default TS Types should be defined" forWord {
    "Int" in { "implicitly[TSType[Int]]" should compile }
    "Long" in { "implicitly[TSType[Int]]" should compile }
    "Double" in { "implicitly[TSType[Double]]" should compile }
    "scala.math.BigDecimal" in { "implicitly[TSType[scala.math.BigDecimal]]" should compile }
    "java.math.BigDecimal" in { "implicitly[TSType[java.math.BigDecimal]]" should compile }
    "scala.math.BigInt" in { "implicitly[TSType[scala.math.BigInt]]" should compile }

    "Tuple2" in {
      "implicitly[TSType[(Int, String)]]" should compile
      val generated = implicitly[TSType[(Int, String)]].get
      val manual = TSTuple.of(implicitly[TSType[Int]].get, implicitly[TSType[String]].get)
      generated should ===(manual)
    }

    "Tuple3" in {
      "implicitly[TSType[(Int, String, Double)]]" should compile
      val generated = implicitly[TSType[(Int, String, Double)]].get
      val manual = TSTuple.of(implicitly[TSType[Int]].get, implicitly[TSType[String]].get, implicitly[TSType[Double]].get)
      generated should ===(manual)
    }

    "Option" in {
      "implicitly[TSType[Option[Int]]]" should compile
      val generated = implicitly[TSType[Option[Int]]].get
      val manual = implicitly[TSType[Int]] | TSNull
      generated should ===(manual)
    }

    "Either" in {
      "implicitly[TSType[Either[Int, String]]]" should compile
      val generated = implicitly[TSType[Either[Int, String]]].get
      val manual = implicitly[TSType[Int]] | implicitly[TSType[String]]
      generated should ===(manual)
    }

    "Map[String, _]" in {
      "implicitly[TSType[Map[String, Int]]]" should compile
    }

    "Map[Int, _]" in {
      "implicitly[TSType[Map[Int, Int]]]" should compile
    }

    "Seq[_]" in {
      "implicitly[TSType[scala.collection.mutable.Seq[Int]]]" should compile
      "implicitly[TSType[scala.collection.immutable.Seq[Int]]]" should compile
      "implicitly[TSType[IndexedSeq[Int]]]" should compile
      "implicitly[TSType[List[Int]]]" should compile
      "implicitly[TSType[Vector[Int]]]" should compile
    }

    "Set[_]" in {
      "implicitly[TSType[Set[Int]]]" should compile
      "implicitly[TSType[scala.collection.immutable.HashSet[Int]]]" should compile
    }

    "Collection<?>" in {
      "implicitly[TSType[java.util.Collection[Int]]]" should compile
      "implicitly[TSType[java.util.List[Int]]]" should compile
      "implicitly[TSType[java.util.ArrayList[Int]]]" should compile
    }
  }

  import scala.language.implicitConversions
  implicit def convertToStringHasWrapperForVerb(o: String)(implicit position: source.Position): HasWrapper =
    new HasWrapper {
      override val leftSideString = o.trim
      override val pos = position
    }

  trait HasWrapper {
    val leftSideString: String
    val pos : source.Position

    def forWord(right: => Unit)(implicit fun: StringVerbBlockRegistration): Unit = {
      fun(leftSideString, "for", pos, right _)
    }
  }
}
