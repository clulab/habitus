package org.clulab.habitus.variables

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.clulab.odin.Mention

class TestAreaReader extends FlatSpec with Matchers {
  val vp = VariableProcessor("/variables/master-areas.yml")

  def getMentions(text: String): Seq[Mention] = {
    val (_, mentions, _, _) = vp.parse(text)
    mentions
  }

  behavior of "AreaReader"

  // TODO: add unit tests here
  //   28,223 ha vs 35,065 ha were used as sown areas
  //   The areas sown for this 2021/2022 wintering campaign are 28,223 ha vs 35,065 ha in wintering
}