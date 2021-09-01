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

  val sent2 = "Sown cultivar and areaJaya: 15.0 haJaya: 27.5 haJaya: 26.6 haSahel 202: 27.5 haSahel 202: 22.5 haSahel 108: 12.5 haSahel 108: 0.9 haJaya: 5.0 haWeedingtype and rate2 l 2-4D ha–1+ 4 l Propanil ha–12 l 2-4D ha–1+ 4 l Propanil ha–1manual2 l 2-4D ha–1+ 4 l Propanil ha–12 l 2-4D ha–1+ 4 l Propanil"
  sent2 should "recognize 4 l"  in {
    val mentions = getMentions(sent2)
    mentions.filter(_.label matches "Assignment") should have size (3)
  }
}
