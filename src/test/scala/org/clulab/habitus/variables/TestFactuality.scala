package org.clulab.habitus.utils

import org.clulab.habitus.variables.VariableProcessor
import org.clulab.odin.Mention
import org.scalatest.{FlatSpec, Matchers}

class TestFactuality extends FlatSpec with Matchers {
  val vp = VariableProcessor("/variables/master-areas.yml")

  def getMentions(text: String): Seq[Mention] = {
    val (_, mentions, _, _) = vp.parse(text)
    mentions
  }

  behavior of "AreaReader"
  val ce = new DefaultContextExtractor()

  val sent1 = "28,223 ha vs 35,065 ha were used as sown areas"
  sent1 should "recognize used as verb and have a factuality score of" in {
    val areaMentions = getMentions(sent1).filter(_.label matches "Assignment")
    areaMentions should have size (1)
    //areaMentions.foreach( m => ce.getFactualityScore(m)_2 should equal "used")

  }

}