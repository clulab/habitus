package org.clulab.habitus.utils

import org.scalactic.source.Position
import org.scalatest.FlatSpec
import org.scalatest.Matchers

class Test extends FlatSpec with Matchers {
  val passingTest = it
  val failingTest = it
  val brokenSyntaxTest = ignore
  val toDiscuss = ignore
  val commentedOut = ignore
  val fixedWithNewProcRelease = ignore

  type Inable = { def in(testFun: => Any)(implicit pos: Position): Unit }
  type Shouldable = { def should(string: String): Inable }
}
