package org.clulab.variables

import org.clulab.odin.{ExtractorEngine, Mention}
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.fastnlp.FastNLPProcessor
import org.scalatest.{FlatSpec, Matchers}

//
// TODO: write tests for all sentences, similar to this: https://github.com/clulab/reach/blob/master/main/src/test/scala/org/clulab/reach/TestActivationEvents.scala
//

class TestVariableReader extends FlatSpec with Matchers {
  val vp = VariableProcessor()

  // the Clu parser breaks on this one, but the SRL works fine!
  val sent1 = "Farmers’ sowing dates ranged from 14 to 31 July for the WS and from 3 to 11 March for the DS."

  def getMentions(text: String): Seq[Mention] = {
    val (_, mentions) = vp.parse(text)
    mentions
  }

  sent1 should "contain two assignment events" in {
    val mentions = getMentions(sent1)

    mentions.filter(_.label matches "Assignment") should have size (2)
    // TODO: make sure they have the right arguments (see TestActivationsEvents for examples)

  }
}
