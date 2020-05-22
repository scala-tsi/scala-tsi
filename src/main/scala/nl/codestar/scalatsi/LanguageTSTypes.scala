package nl.codestar.scalatsi

import nl.codestar.scalatsi.TypescriptType._

trait ScalaTSTypes {
  implicit val anyTSType: TSType[Any] = TSType(TSAny)
}

trait CollectionTSTypes extends LowPriorityCollectionTSType {
  // This chooses null union to represent Option types.
  // When defining interfaces however Option will be represented with undefined union
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
  // Provides a TSType for any scala collection of E to a typescript array of E
  implicit def tsTraversable[E, F[_]](implicit
    e: TSType[E],
    ev: F[E] <:< Iterable[E]
  ): TSType[F[E]] =
    TSType(e.get.array)
}

trait JavaTSTypes {
  implicit val javaObjectTSType: TSType[Object] = TSType(TSObject)

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
    ev: F[E] <:< java.util.Collection[E]
  ): TSType[F[E]] = TSType(e.get.array)

  implicit val javaUriTSType: TSType[java.net.URI]    = TSType(TSString)
  implicit val javaUrlTSType: TSType[java.net.URL]    = TSType(TSString)
  implicit val javaUuidTSType: TSType[java.util.UUID] = TSType(TSString)
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
