package org.clulab.habitus.variables


import org.clulab.habitus.variables.VariableReader.extractContext
import org.scalatest.{FlatSpec, Matchers}

//
// TODO: write tests for all sentences, similar to this: https://github.com/clulab/reach/blob/master/main/src/test/scala/org/clulab/reach/TestActivationEvents.scala
//

class TestContextExtractor extends FlatSpec with Matchers {
  val vp = VariableProcessor()
  val ce = EntityHistogramExtractor()
  val vr= VariableReader

//pass1: test sentences which have only one event overall in the document
  val sent1 ="""
In Matto Grosso  with sowing  between 7 and 22 July, in  maturity came in early November ( Tab.I ) .
In United States , United States  maturity came in early November ( Tab.I ) .
In Senegal maturity  came in early November ( Tab.I ) for 1995.
In Senegal maturity  came in early November ( Tab.I ) for 1995.
In Senegal maturity  came in early November ( Tab.I ) for 1995.
"""

  def getMSFreq(text: String): Seq[vr.MostFreqEntity] = {
    val (doc, mentions,allEventMentions, histogram) = vp.parse(text)
    val mse=vr.extractContext(doc,allEventMentions,Int.MaxValue,"LOC",histogram)
    (mse)
  }

  sent1 should "for the event sowing between 7 and 22 July find senegal as the most frequent entity in entire document" in {
    val mse = getMSFreq(sent1)
    mse.head.mostFreqEntity.getOrElse("") should be ("senegal")
  }
  def getMSFreq1Sent(text: String): Seq[vr.MostFreqEntity] = {
    val (doc, mentions,allEventMentions, histogram) = vp.parse(text)
    val mse=vr.extractContext(doc,allEventMentions,1,"LOC",histogram)
    (mse)
  }

  sent1 should "find united states as the most frequent entity within 1 sentence distance" in {
    val mse = getMSFreq1Sent(sent1)
    mse.head.mostFreqEntity.getOrElse("")  should be ("united states")
  }


  def getMSFreq0Sent(text: String): Seq[vr.MostFreqEntity] = {
    val (doc, mentions,allEventMentions, histogram) = vp.parse(text)
    val mse=vr.extractContext(doc,allEventMentions,0,"LOC",histogram)
    (mse)
  }


  def getMostFreqYearOverall(text: String): Seq[vr.MostFreqEntity] = {
    val (doc, mentions,allEventMentions, histogram) = vp.parse(text)
    val mse=vr.extractContext(doc,allEventMentions,Int.MaxValue,"DATE",histogram)
    (mse)
  }


  sent1 should "find matto grosso  as the most frequent entity within 0 sentence distance" in {
    val mse = getMSFreq0Sent(sent1)
    mse.head.mostFreqEntity.getOrElse("")  should be ("matto grosso")
  }

  sent1 should "find 1995 as the most frequently mentioned year overall in the document" in {
    val mse = getMostFreqYearOverall(sent1)
    mse.head.mostFreqEntity.getOrElse("")  should be ("1995")
  }


  // test document which have more than one event mention  in the document
  val sent2 ="""
In Matto Grosso  with sowing between 7 and 22 July, in  maturity came in early November ( Tab.I ) .
In United States , United States  maturity came in early November ( Tab.I ) .
In Senegal maturity  came in early November ( Tab.I ) for 1995.
In Senegal maturity  came in early November ( Tab.I ) for 1995.
In Burkino Faso with sowing between 8 and 12 July, in  maturity came in early November ( Tab.I ) .
In Senegal maturity  came in early November ( Tab.I ) for 1995.
"""

  sent2 should "for document with two events find senegal as the most frequent entity in entire document" in {
    val mse = getMSFreq(sent2)
    mse(0).mostFreqEntity.getOrElse("")  should be ("senegal")
    mse(1).mostFreqEntity.getOrElse("")  should be ("senegal")
  }

  sent2 should "for document with two events find matto grosso as the most frequent entity in event1 and burkino faso for event 0" in {
    val mse = getMSFreq0Sent(sent2)
    val mse0 = mse(0).mostFreqEntity.getOrElse("") // used to be "matto grosso"
    val mse1 = mse(1).mostFreqEntity.getOrElse("") // used to be "burkino faso"
    val mseSeq = Seq(mse0, mse1).sorted

    // The frequencies are tied, so it is essential to have sorted them before the comparison.
    mseSeq(0) should be ("burkino faso")
    mseSeq(1) should be ("matto grosso")
  }
}
