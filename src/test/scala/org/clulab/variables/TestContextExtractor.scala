package org.clulab.variables

import org.clulab.odin.Mention
import org.scalatest.{FlatSpec, Matchers}

//
// TODO: write tests for all sentences, similar to this: https://github.com/clulab/reach/blob/master/main/src/test/scala/org/clulab/reach/TestActivationEvents.scala
//

class TestContextExtractor extends FlatSpec with Matchers {
  val vp = VariableProcessor()


  val sent1 ="""
In Matto Grosso, United States  with sowing between 7 and 22 July, in  maturity came in early November ( Tab.I ) .
In United States , United States and  Senegal  maturity came in early November ( Tab.I ) .
In Senegal maturity  came in early November ( Tab.I ) for 1995.
"""

  def getMSFreq(text: String): Seq[String] = {
    val (doc, mentions) = vp.parse(text)
    val mse=vp.extractContextAndFindMostFrequentEntity(doc,mentions,Int.MaxValue,"LOC")
    (mse)
  }

  sent1 should "find senegal as the most frequent entity overall" in {
    val mse = getMSFreq(sent1)
    mse.head should be ("senegal")
  }


}
