package org.clulab.beliefs

import org.clulab.habitus.utils.Test
import org.clulab.odin.Mention
import org.scalatest.{FlatSpec, Matchers}

class TestBeliefReader extends Test {
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
  sent2 should "contain no belief" in {
    val mentions = getMentions(sent2)
    mentions should have size(0)

    // ms: we do not allow beliefs that are not propositions
    //val m = mentions.head
    //m.arguments("believer").head.text should be ("I")
    //m.arguments("belief").head.text should be ("Trump")
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

  val sent10 = "It's credible that Rudy Giuliani broke the law"
  sent10 should "contain one belief" in {
    val mentions = getMentions(sent10)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("It")
    m.arguments("belief").head.text should be ("Rudy Giuliani broke the law")
  }

  val sent10_0 = "It's not credible that Rudy Giuliani broke the law"
  sent10_0 should "contain one belief" in {
    val mentions = getMentions(sent10_0)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("It")
    m.arguments("belief").head.text should be ("Rudy Giuliani broke the law")
  }

  val sent10_1 = "It's convincing that Rudy Giuliani broke the law"
  sent10_1 should "contain one belief" in {
    val mentions = getMentions(sent10_1)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("It")
    m.arguments("belief").head.text should be ("Rudy Giuliani broke the law")
  }

  val sent10_2 = "It's believable that Rudy Giuliani broke the law"
  sent10_2 should "contain one belief" in {
    val mentions = getMentions(sent10_2)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("It")
    m.arguments("belief").head.text should be ("Rudy Giuliani broke the law")
  }

  val sent10_3 = "It's inconceivable that anyone would hate The Princess Bride"
  sent10_3 should "contain one belief" in {
    val mentions = getMentions(sent10_3)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("It")
    m.arguments("belief").head.text should be ("anyone would hate The Princess Bride")
  }

  val sent10_4 = "It's suspect that Rudy Giuliani broke the law"
  sent10_4 should "contain one belief" in {
    val mentions = getMentions(sent10_4)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("It")
    m.arguments("belief").head.text should be ("Rudy Giuliani broke the law")
  }

  val sent10_5 = "It's plausible that Rudy Giuliani broke the law"
  sent10_5 should "contain one belief" in {
    val mentions = getMentions(sent10_5)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("It")
    m.arguments("belief").head.text should be ("Rudy Giuliani broke the law")
  }

  val sent10_6 = "It's questionable that anyone would hate The Princess Bride"
  sent10_6 should "contain one belief" in {
    val mentions = getMentions(sent10_6)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("It")
    m.arguments("belief").head.text should be ("anyone would hate The Princess Bride")
  }

 val sent10_7 = "It's dubious that anyone would hate The Princess Bride"
  sent10_7 should "contain one belief" in {
    val mentions = getMentions(sent10_7)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("It")
    m.arguments("belief").head.text should be ("anyone would hate The Princess Bride")
  }

  val sent10_8 = "It's equivocal that anyone would hate The Princess Bride"
  sent10_8 should "contain one belief" in {
    val mentions = getMentions(sent10_8)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("It")
    m.arguments("belief").head.text should be ("anyone would hate The Princess Bride")
  }

  val sent10_9 = "It's conclusive that anyone would hate The Princess Bride"
  sent10_9 should "contain one belief" in {
    val mentions = getMentions(sent10_9)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("It")
    m.arguments("belief").head.text should be ("anyone would hate The Princess Bride")
  }

  val sent10_10 = "It's false that anyone would hate The Princess Bride"
  sent10_10 should "contain one belief" in {
    val mentions = getMentions(sent10_10)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("It")
    m.arguments("belief").head.text should be ("anyone would hate The Princess Bride")
  }

  val sent11 = "The New York Times is skeptical of Trump’s claims"
  sent11 should "contain one belief" in {
    val mentions = getMentions(sent11)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("New York Times")
    m.arguments("belief").head.text should be ("Trump’s claims")
  }

  // fixme: expand on nmod_than
   val sent13 = "There’s no reason to think that Mu is worse than Delta"
   failingTest should s"contain one belief in ${sent13} " in {
     val mentions = getMentions(sent13)
     mentions should have size(1)
     val m = mentions.head
     m.arguments("belief").head.text should be ("Mu is worse than Delta")
   }

  val sent14_1 = "Violent storms are consistent with climate change"
  sent14_1 should "contain one belief" in {
    val mentions = getMentions(sent14_1)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Violent storms")
    m.arguments("belief").head.text should be ("climate change")
  }

  val sent14_2 = "Violent storms are explained by climate change"
  sent14_2 should "contain one belief" in {
    val mentions = getMentions(sent14_2)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Violent storms")
    m.arguments("belief").head.text should be ("climate change")
  }

  val sent14_3 = "Violent storms are predicted by climate change"
  sent14_3 should "contain one belief" in {
    val mentions = getMentions(sent14_3)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Violent storms")
    m.arguments("belief").head.text should be ("climate change")
  }

 val sent14_4 = "It's hard to see how burning coal helps the environment"
  sent14_4 should "contain one belief" in {
    val mentions = getMentions(sent14_4)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("It")
    m.arguments("belief").head.text should be ("how burning coal helps the environment")
  }

  val sent15 = "Trump is thought to be a narcissist"
  sent15 should "contain one belief" in {
    val mentions = getMentions(sent15)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("beliefTheme").head.text should be ("Trump")
    m.arguments("belief").head.text should be ("narcissist")
  }

  val sent16 = "Paul is sure that bleach kills covid."
  sent16 should "contain one belief" in {
    val mentions = getMentions(sent16)
    mentions should have size(1)

        val m = mentions.head
        m.arguments("believer").head.text should be ("Paul")
        m.arguments("belief").head.text should be ("bleach kills covid")
  }

  // fixme: full mention span does not include `sterility`
  val sent17 = "Paul is unsure about whether covid vaccination causes sterility."
  sent17 should "contain one belief" in {
    val mentions = getMentions(sent17)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Paul")
    m.arguments("belief").head.text should be ("covid vaccination causes sterility")
  }

  val sent18 = "Paul questions the popular theory that Trump is an idiot."
  brokenSyntaxTest should s"contain one belief in ${sent18}" in {
    val mentions = getMentions(sent18)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Paul")
    m.arguments("belief").head.text should be ("Trump is an idiot")
  }



  // TODO: Returns many believes associations
   val sent19 = "Senator Harris mistrusts Trump’s medical recommendations but trusts Fauci’s"
   failingTest should s"contain one belief in ${sent19}" in {
     val mentions = getMentions(sent19)
     mentions should have size(1)

     val m = mentions.head
     m.arguments("believer").head.text should be ("Senator Harris")
     m.arguments("belief").head.text should be ("Trump’s medical recommendations")
   }

}

