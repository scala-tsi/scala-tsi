package com.scalatsi

import TypescriptType.*

import scala.annotation.unused
import scala.collection.immutable.ListMap
import scala.reflect.ClassTag

trait ScalaTSTypes {
  implicit val anyTSType: TSType[Any]   = TSType(TSAny)
  implicit val unitTsType: TSType[Unit] = TSType(TSVoid)
}

trait CollectionTSTypes extends LowPriorityCollectionTSType {

  /** Represent an Option[E] with a Typescript (E | undefined) */
  implicit def tsOption[E](implicit e: TSType[E]): TSType[Option[E]] = TSType(e | TSUndefined)
  implicit val noneTSType: TSType[None.type]                         = TSType(TSNull)
  implicit def tsSome[E](implicit e: TSType[E]): TSType[Some[E]]     = TSType(e.get)

  implicit def tsEither[L, R](implicit tsLeft: TSType[L], tsRight: TSType[R]): TSType[Either[L, R]] = TSType(tsLeft | tsRight)

  implicit def tsStringMap[E](implicit e: TSType[E]): TSType[Map[String, E]] =
    TSType(TSIndexedInterface(indexType = TSString, valueType = e.get))

  implicit def tsIntMap[E](implicit e: TSType[E]): TSType[Map[Int, E]] =
    TSType(TSIndexedInterface(indexType = TSNumber, valueType = e.get))
}

trait LowPriorityCollectionTSType {

  /** Provides a TSType for any scala collection of E to a typescript array of E */
  implicit def tsTraversable[E, F[_]](implicit e: TSType[E], @unused ev: F[E] <:< Iterable[E]): TSType[F[E]] = TSType(e.get.array)
}

trait ScalaEnumTSTypes {
  implicit def scalaEnumTSType[E <: Enumeration: ValueOf]: TSNamedType[E] = {
    val e                   = valueOf[E]
    val name: String        = e.toString()
    val values: Seq[String] = e.values.map(id => id.toString).toSeq

    TSType.alias[E](name, TSUnion(values.map(TSLiteralString.apply)))
  }
}

trait JavaTSTypes {
  implicit val javaObjectTSType: TSType[Object] = TSType(TSObject)
  implicit val javaVoidTSType: TSType[Void]     = TSType(TSVoid)

  import java.time.temporal.Temporal
  // Most JSON serializers write java.time times to a ISO8601-like string
  // Epoch (milli)seconds are also common, in this case users will need to provide their own TSType[TheirTimeRepresentation]
  // Should regex typescript types be implemented (https://github.com/Microsoft/TypeScript/issues/6579),
  // we could define more specific formats for the varying dates and times
  /** Type to serialize java.time.* dates/times and java.util.Date to, override this to change your representation */
  protected val java8TimeTSType: TSType[Temporal]     = TSType(TSString)
  implicit val javaDateTSType: TSType[java.util.Date] = java8TimeTSType.asInstanceOf[TSType[java.util.Date]]

  implicit def java8DateTSTypeConversion[T <: Temporal]: TSType[T] = java8TimeTSType.asInstanceOf[TSType[T]]

  implicit def javaNumber[T <: java.lang.Number]: TSType[T] = TSType(TSNumber)

  // All java collection types implement Collection and are almost always translated to javascript arrays
  implicit def tsJavaCollection[E, F[_]](implicit
      e: TSType[E],
      @unused ev: F[E] <:< java.util.Collection[E]
  ): TSType[F[E]] = TSType(e.get.array)

  implicit val javaUriTSType: TSType[java.net.URI]    = TSType(TSString)
  implicit val javaUrlTSType: TSType[java.net.URL]    = TSType(TSString)
  implicit val javaUuidTSType: TSType[java.util.UUID] = TSType(TSString)

  implicit def javaEnumTSType[E <: java.lang.Enum[E]: ClassTag]: TSNamedType[E] = {

    val cls = implicitly[ClassTag[E]].runtimeClass
    val values = Option(cls.getEnumConstants)
      .getOrElse(throw new IllegalStateException(s"Expected ${cls.getCanonicalName} to be a java.lang.Enum, it was not"))
      .asInstanceOf[Array[E]]
      .toSeq

    TSType.alias[E](
      cls.getSimpleName,
      TSUnion(values.map(v => TSLiteralString(v.name())))
    )
  }
}

trait TupleTSTypes {
  implicit def tsTuple1[T1](implicit t1: TSType[T1]): TSType[Tuple1[T1]] =
    TSType(TSTuple.of(t1.get))
  implicit def tsTuple2[T1, T2](implicit t1: TSType[T1], t2: TSType[T2]): TSType[(T1, T2)] =
    TSType(TSTuple.of(t1.get, t2.get))
  implicit def tsTuple3[T1, T2, T3](implicit t1: TSType[T1], t2: TSType[T2], t3: TSType[T3]): TSType[(T1, T2, T3)] =
    TSType(TSTuple.of(t1.get, t2.get, t3.get))
  implicit def tsTuple4[T1, T2, T3, T4](implicit t1: TSType[T1], t2: TSType[T2], t3: TSType[T3], t4: TSType[T4]): TSType[(T1, T2, T3, T4)] =
    TSType(TSTuple.of(t1.get, t2.get, t3.get, t4.get))
  implicit def tsTuple5[T1, T2, T3, T4, T5](implicit
      t1: TSType[T1],
      t2: TSType[T2],
      t3: TSType[T3],
      t4: TSType[T4],
      t5: TSType[T5]
  ): TSType[(T1, T2, T3, T4, T5)] =
    TSType(TSTuple.of(t1.get, t2.get, t3.get, t4.get, t5.get))
  implicit def tsTuple6[T1, T2, T3, T4, T5, T6](implicit
      t1: TSType[T1],
      t2: TSType[T2],
      t3: TSType[T3],
      t4: TSType[T4],
      t5: TSType[T5],
      t6: TSType[T6]
  ): TSType[(T1, T2, T3, T4, T5, T6)] =
    TSType(TSTuple.of(t1.get, t2.get, t3.get, t4.get, t5.get, t6.get))
  // TODO: Tuple7-21
}

// TODO: Can we use reflection or macros to get the real argument names?
trait FunctionTSTypes {
  implicit def tsFunction0[R](implicit r: TSType[R]): TSType[() => R] =
    TSType(TSFunction(ListMap(), r.get))

  implicit def tsFunction1[P0, R](implicit t1: TSType[P0], r: TSType[R]): TSType[P0 => R] =
    TSType(TSFunction(ListMap("arg0" -> t1.get), r.get))

  implicit def tsFunction2[P0, P1, R](implicit t1: TSType[P0], t2: TSType[P1], r: TSType[R]): TSType[(P0, P1) => R] =
    TSType(TSFunction(ListMap("arg0" -> t1.get, "arg1" -> t2.get), r.get))

  implicit def tsFunction3[P0, P1, P2, R](implicit
      t1: TSType[P0],
      t2: TSType[P1],
      t3: TSType[P2],
      r: TSType[R]
  ): TSType[(P0, P1, P2) => R] =
    TSType(TSFunction(ListMap("arg0" -> t1.get, "arg1" -> t2.get, "arg2" -> t3.get), r.get))

  implicit def tsFunction4[P0, P1, P2, P3, R](implicit
      t1: TSType[P0],
      t2: TSType[P1],
      t3: TSType[P2],
      t4: TSType[P3],
      r: TSType[R]
  ): TSType[(P0, P1, P2, P3) => R] =
    TSType(TSFunction(ListMap("arg0" -> t1.get, "arg1" -> t2.get, "arg2" -> t3.get, "arg3" -> t4.get), r.get))

  // TODO: Function5-22
}
