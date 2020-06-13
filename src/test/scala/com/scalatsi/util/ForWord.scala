package com.scalatsi.util

import org.scalactic.source
import org.scalatest.words.StringVerbBlockRegistration

trait ForWord {
  val leftSideString: String
  val pos: source.Position

  def forWord(right: => Unit)(implicit fun: StringVerbBlockRegistration): Unit = {
    fun(leftSideString, "for", pos, () => right)
  }
}
