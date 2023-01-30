package com.scalatsi

import scala.deriving.Mirror

private[scalatsi] object MetaUtil {
  def tsName[T](using m : Mirror.Of[T]): String =

}
