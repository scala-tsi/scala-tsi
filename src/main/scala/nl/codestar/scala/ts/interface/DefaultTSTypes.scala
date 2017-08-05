package nl.codestar.scala.ts.interface

import nl.codestar.scala.ts.interface.TypescriptType._

trait DefaultTSTypes
    extends PrimitiveTSTypes
    with GenericTSTypes
    with TupleTSTypes {

  implicit def seqTsType[E](implicit e: TSType[E]): TSType[Seq[E]] =
    TSType(TSArray(e.get))
  implicit def optionTsType[E](implicit e: TSType[E]): TSType[Option[E]] =
    TSType(TSUnion.of(e.get, TSUndefined))
}
object DefaultTSTypes extends DefaultTSTypes

trait PrimitiveTSTypes {
  implicit val BooleanTsType: TSType[Boolean] = TSType(TSBoolean)
  implicit val StringTsType: TSType[String] = TSType(TSString)
  implicit def NumberTsType[T: Numeric]: TSType[T] = TSType(TSNumber)
}

trait GenericTSTypes {
  // All scala collection types implement Traversable and are almost always translated to javascript arrays
  implicit def tsTraversable[E](
      implicit e: TSType[E]): TSType[Traversable[E]] = TSType(TSArray(e.get))
  // All java collection types implement Collection and are almost always translated to javascript arrays
  implicit def tsJavaCollection[E](
      implicit e: TSType[E]): TSType[java.util.Collection[E]] =
    TSType(TSArray(e.get))
  // This chooses null union to represent Option types.
  // When defining interfaces however Option will be represented with undefined union
  implicit def tsOption[E](implicit e: TSType[E]): TSType[Option[E]] =
    TSType(TSUnion.of(e.get, TSNull))
}

trait TupleTSTypes {
  implicit def tsTuple1[T1](implicit t1: TSType[T1]): TSType[Tuple1[T1]] =
    TSType(TSTuple.of(t1.get))
  implicit def tsTuple2[T1, T2](implicit t1: TSType[T1],
                                t2: TSType[T2]): TSType[(T1, T2)] =
    TSType(TSTuple.of(t1.get, t2.get))
  implicit def tsTuple3[T1, T2, T3](implicit t1: TSType[T1],
                                    t2: TSType[T2],
                                    t3: TSType[T3]): TSType[(T1, T2, T3)] =
    TSType(TSTuple.of(t1.get, t2.get, t3.get))
  implicit def tsTuple4[T1, T2, T3, T4](
      implicit t1: TSType[T1],
      t2: TSType[T2],
      t3: TSType[T3],
      t4: TSType[T4]): TSType[(T1, T2, T3, T4)] =
    TSType(TSTuple.of(t1.get, t2.get, t3.get, t4.get))
  // TODO: Tuple5-21
}
