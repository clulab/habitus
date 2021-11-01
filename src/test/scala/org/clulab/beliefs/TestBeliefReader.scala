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

  // TODO: Mentions of this sentence has size 2
  
  // val sent3 = "Most women think Trump is a bad person"
  // sent3 should "contain one belief" in {
  //   val mentions = getMentions(sent3)
  //   mentions should have size(1)

  //   val m = mentions.head
  //   m.arguments("believer").head.text should be ("Most women")
  //   m.arguments("belief").head.text should be ("Trump is a bad person")
  // }

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

  // Hubert
  // It’s credible / not credible / convincing / unconvincing
  //  / believable / likely / unlikely / provable / possible /
  //   impossible / probable / improbable / plausible /
  //   implausible / reasonable / unreasonable / conclusive /
  //   unsupportable / questionable / unquestionable / axiomatic /
  //    true / false / established / debatable / equivocal / 
  //    unequivocal / dubious / suspect  / incontrovertible / 
  //    indubitable that Rudy Giuliani broke the law

  val sent10 = "It's credible that Rudy Giuliani broke the law"
  sent10 should "contain one belief" in {
    val mentions = getMentions(sent10)
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

  // TODO: Look at this trigger expression which is a Compound Words "no reason to think"
  // val sent13 = "There’s no reason to think that Mu is worse than Delta"
  // sent13 should "contain one belief" in {
  //   val mentions = getMentions(sent13)
  //   mentions should have size(1)

  //   val m = mentions.head
  //   m.arguments("believer").head.text should be ("I")
  //   m.arguments("belief").head.text should be ("Rudy Giuliani broke the law")
  // }


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

  // TODO: I am wondering if the trigger should be "hard to see" or only "hard"
  // val sent15 = "It’s hard to see how burning coal helps the environment"
  // sent15 should "contain one belief" in {
  //   val mentions = getMentions(sent15)
  //   mentions should have size(1)

  //   val m = mentions.head
  //   m.arguments("believer").head.text should be ("Its")
  //   m.arguments("belief").head.text should be ("burning coal helps the environment")
  // }

  // TODO: Returns many believes associations
  // val sent16 = "Senator Harris mistrusts Trump’s medical recommendations but trusts Fauci’s"
  // sent16 should "contain one belief" in {
  //   val mentions = getMentions(sent16)
  //   mentions should have size(1)

  //   val m = mentions.head
  //   m.arguments("believer").head.text should be ("Senator Harris")
  //   m.arguments("belief").head.text should be ("Trump’s medical recommendations")
  // }

}