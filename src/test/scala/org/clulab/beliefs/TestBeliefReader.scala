package org.clulab.beliefs

import org.clulab.odin.Mention
import org.scalatest.{FlatSpec, Matchers}

class TestBeliefReader extends FlatSpec with Matchers {
  val bp = BeliefProcessor()

  def getMentions(text: String): Seq[Mention] = {
    val (_, mentions) = bp.parse(text)
    mentions.filter(_.label matches "Belief")
  }

  val sent1 = "farmers believe that loans are useful."
  sent1 should "contain one belief" in {
    val mentions = getMentions(sent1)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("farmers")
    m.arguments("belief").head.text should be ("loans are useful")
  }

  val sent2 = "I believe Trump"
  sent2 should "contain one belief" in {
    val mentions = getMentions(sent2)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("I")
    m.arguments("belief").head.text should be ("Trump")
  }

  val sent3 = "Most women think Trump is a bad person"
  sent3 should "contain one belief" in {
    val mentions = getMentions(sent3)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Most women")
    m.arguments("belief").head.text should be ("Trump is a bad person")
  }

  val sent4 = "Allegra accepts that Paul makes good beer"
  sent4 should "contain one belief" in {
    val mentions = getMentions(sent4)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Allegra")
    m.arguments("belief").head.text should be ("Paul makes good beer")
  }

  val sent5 = "Allegra considers that Dogfish Head makes better beer"
  sent5 should "contain one belief" in {
    val mentions = getMentions(sent5)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Allegra")
    m.arguments("belief").head.text should be ("Dogfish Head makes better beer")
  }

  val sent6 = "Allegra concludes that Paul makes the best beer"
  sent6 should "contain one belief" in {
    val mentions = getMentions(sent6)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Allegra")
    m.arguments("belief").head.text should be ("Paul makes the best beer")
  }

  val sent7 = "Daniel doubts that there will be more virulent mutations"
  sent7 should "contain one belief" in {
    val mentions = getMentions(sent7)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Daniel")
    m.arguments("belief").head.text should be ("there will be more virulent mutations")
  }

  val sent8 = "I reject the premises of Social Darwinism"
  sent8 should "contain one belief" in {
    val mentions = getMentions(sent8)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("I")
    m.arguments("belief").head.text should be ("premises of Social Darwinism")
  }

  val sent9 = "I suppose that Troeg’s might know a thing or two about beer"
  sent9 should "contain one belief" in {
    val mentions = getMentions(sent9)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("I")
    m.arguments("belief").head.text should be ("Troeg’s might know a thing or two about beer")
  }

}
