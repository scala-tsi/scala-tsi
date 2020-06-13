package com

import com.scalatsi.util.ForWord
import org.scalactic.source

package object scalatsi {
  import scala.language.implicitConversions

  /** Enable a `"something" forWord {}` test */
  implicit def convertToStringHasWrapperForVerb(o: String)(implicit position: source.Position): ForWord =
    new ForWord {
      final override val leftSideString = o.trim
      final override val pos            = position
    }

}
