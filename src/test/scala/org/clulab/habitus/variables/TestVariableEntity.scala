package org.clulab.habitus.variables

import org.clulab.habitus.utils.Test
import org.clulab.odin.Mention

class TestVariableEntity extends Test {
  val vp: VariableProcessor = VariableProcessor()

  def getMentions(text: String): Seq[Mention] = {
    val parsingResults = vp.parse(text)
    parsingResults.targetMentions
  }

  behavior of "VariableReader"

  val sent1 = "35,065 ha was harvested in the rainy season"
  sent1 should "recognize season and yield amount" in {
    val varMentions = getMentions(sent1)
    varMentions should have size (2)
    varMentions.head.arguments("value").head.text should equal("35,065 ha")
    varMentions.head.arguments("variable").head.text should equal("rainy season")
  }
}

