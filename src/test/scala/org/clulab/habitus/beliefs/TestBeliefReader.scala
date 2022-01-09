package org.clulab.habitus.beliefs

import org.clulab.habitus.utils.Test
import org.clulab.odin.Mention

class TestBeliefReader extends Test {
  val bp = BeliefProcessor()

  def getMentions(text: String): Seq[Mention] = {
    val (_, mentions) = bp.parse(text)
    // check all labels for Belief to account for LikelyFact beliefs
    mentions.filter(_.labels contains "Belief")
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
  toDiscuss should s"contain one belief in ${sent4}" in {
    val mentions = getMentions(sent4)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Allegra")
    m.arguments("belief").head.text should be ("Paul makes good beer")
  }

  val sent5 = "Allegra considers that Dogfish Head makes better beer"
  toDiscuss should s"contain one belief in ${sent5}" in {
    val mentions = getMentions(sent5)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Allegra")
    m.arguments("belief").head.text should be ("Dogfish Head makes better beer")
  }

  val sent6 = "Allegra concludes that Paul makes the best beer"
  toDiscuss should s"contain one belief in ${sent6}" in {
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
  toDiscuss should s"contain one belief in ${sent8}" in {
    val mentions = getMentions(sent8)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("I")
    m.arguments("belief").head.text should be ("premises of Social Darwinism")
  }

  val sent9 = "I suppose that Tröegs might know a thing or two about beer"
  sent9 should "contain one belief" in {
    val mentions = getMentions(sent9)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("I")
    m.arguments("belief").head.text should be ("Tröegs might know a thing or two about beer")
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
  toDiscuss should s"contain one belief in ${sent10_1}" in {
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
  toDiscuss should s"contain one belief in ${sent10_4}" in {
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
  toDiscuss should s"contain one belief in ${sent10_8}" in {
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
  toDiscuss should s"contain one belief in ${sent10_10}" in {
    val mentions = getMentions(sent10_10)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("It")
    m.arguments("belief").head.text should be ("anyone would hate The Princess Bride")
  }

  val sent11 = "The New York Times is skeptical of Trump’s claims"
  toDiscuss should s"contain one belief in ${sent11}" in {
    val mentions = getMentions(sent11)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("New York Times")
    m.arguments("belief").head.text should be ("Trump’s claims")
  }

  // fixme: expand on nmod_than
   val sent13 = "There’s no reason to think that Mu is worse than Delta"
   failingTest should s"contain one belief in '${sent13}'" in {
     val mentions = getMentions(sent13)
     mentions should have size(1)
     val m = mentions.head
     m.arguments("belief").head.text should be ("Mu is worse than Delta")
   }

  val sent14_1 = "Violent storms are consistent with climate change"
  toDiscuss should s"contain one belief in ${sent14_1}" in {
    val mentions = getMentions(sent14_1)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Violent storms")
    m.arguments("belief").head.text should be ("climate change")
  }

  val sent14_2 = "Violent storms are explained by climate change"
  toDiscuss should s"contain one belief in ${sent14_2}" in {
    val mentions = getMentions(sent14_2)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Violent storms")
    m.arguments("belief").head.text should be ("climate change")
  }

  val sent14_3 = "Violent storms are predicted by climate change"
  toDiscuss should s"contain one belief in ${sent14_3}" in {
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
  brokenSyntaxTest should s"contain one belief in '${sent18}'" in {
    val mentions = getMentions(sent18)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Paul")
    m.arguments("belief").head.text should be ("Trump is an idiot")
  }

  // TODO: Returns many believes associations
   val sent19 = "Senator Harris mistrusts Trump’s medical recommendations but trusts Fauci’s"
   failingTest should s"contain one belief in '${sent19}'" in {
     val mentions = getMentions(sent19)
     mentions should have size(1)

     val m = mentions.head
     m.arguments("believer").head.text should be ("Senator Harris")
     m.arguments("belief").head.text should be ("Trump’s medical recommendations")
     // Mistrust is particularly high in Senegal
     //(83a) and Liberia (78P‹).}
   }

  val sent20 = "Only three in 10 respondents (31a) say they trust their government somewhat or a lot to ensure that any vaccine is safe before it is offered to citizens.."
  failingTest should s"contain one belief in ${sent20}" in {
    val mentions = getMentions(sent20)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be("three in 10 respondents")
    m.arguments("belief").head.text should be ("their government somewhat or a lot to ensure that any vaccine is safe")
  }

  val sent21 = "I expect my investments to do well"
  failingTest should s"contain one belief in '${sent21}'" in {
    val mentions = getMentions(sent21)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("I")
    m.arguments("belief").head.text should be ("my investments to do well")

  }

  val sent22 = "I look forward to my investments doing well"
  toDiscuss should s"contain one belief in '${sent22}'" in {
    val mentions = getMentions(sent22)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("I")
    m.arguments("belief").head.text should be ("my investments doing well")

  }

  val sent23 = "We are all worried about global warming."
  failingTest should s"contain one belief in '${sent23}'" in {
    val mentions = getMentions(sent23)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("We")
    m.arguments("belief").head.text should be ("global warming")

  }

  val sent24 = "The IPCC is pessimistic about countries meeting their Paris targets."
  toDiscuss should s"contain one belief in '${sent24}'" in {
    val mentions = getMentions(sent24)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("IPCC")
    m.arguments("belief").head.text should be ("countries meeting their Paris targets")

  }

  val sent25 = "We fear another Trump presidency."
  toDiscuss should s"contain one belief in '${sent25}'" in {
    val mentions = getMentions(sent25)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("We")
    m.arguments("belief").head.text should be ("Trump presidency")

  }

  val sent26 = "Paul is hopeful that Breyer will step down."
  passingTest should s"contain one belief in '${sent26}'" in {
    val mentions = getMentions(sent26)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Paul")
    m.arguments("belief").head.text should be ("Breyer will step") // fixme: should incl `down`

  }

  val sent27 = "We are optimistic about the future of this research."
  passingTest should s"contain one belief in '${sent27}'" in {
    val mentions = getMentions(sent27)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("We")
    m.arguments("belief").head.text should be ("future of this research")

  }

  val sent28 = "The Government of Senegal and other key stakeholders acknowledge that a major issue in the country is that there is a lack of effective concerted planning of the climate change efforts."
  toDiscuss should s"contain one belief in '${sent28}'" in {
    val mentions = getMentions(sent28)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Government of Senegal and other key stakeholders")
    m.arguments("belief").head.text should be ("major issue in the country is that there is a lack of effective concerted planning")

  }

//  val sent29 = "the Government of Senegal is giving highest priority to rice self-sufficiency"
//  passingTest should s"contain one belief in '${sent29}'" in {
//    val mentions = getMentions(sent29)
//    mentions should have size(1)
//
//    val m = mentions.head
//    m.arguments("believer").head.text should be ("Government of Senegal")
//    m.arguments("belief").head.text should be ("rice self-sufficiency")
//
//  }

  val sent30 = "Although progress in the art and science of seasonal forecasting and other climate information services is being made, ANACIM acknowledges that the approximately 70% accuracy of its seasonal forecasts leave considerable room for improvement."
  toDiscuss should s"contain one belief in '${sent30}'" in {
    val mentions = getMentions(sent30)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("ANACIM")
    m.arguments("belief").head.text should be ("70% accuracy of its seasonal forecasts leave considerable room for improvement")

  }

  val sent31 = "It is very positive that the Mission has already begun to identify and fill some initial critical gaps related to the context-specific climate change risks in Senegal in its Feed the Future portfolio"
  toDiscuss should s"contain one belief in '${sent31}'" in {
    val mentions = getMentions(sent31)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("belief").head.text should be ("Mission has already begun to identify and fill some initial critical gaps related to the context-specific climate change risks")

  }

  // this is probably preference
//  val sent32 = "Most interviewed farmers expressed the desire and ambition to free up time to participate in family and community events."
//  passingTest should s"contain one belief in '${sent32}'" in {
//    val mentions = getMentions(sent32)
//    mentions should have size(1)
//
//    val m = mentions.head
//    m.arguments("believer").head.text should be ("interviewed farmers")
//    m.arguments("belief").head.text should be ("desire and ambition to free up time to participate in family and community events")
//
//  }

  val sent33 = "Farmers perceived that rice intensification was driven by political and financial incentives"
  toDiscuss should s"contain one belief in '${sent33}'" in {
    val mentions = getMentions(sent33)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Farmers")
    m.arguments("belief").head.text should be ("rice intensification was driven by political and financial incentives")

  }

  val sent34 = "The farmers stated that the low production levels of crops and post-harvest losses originated primarily from lack of well-functioning machinery, storage facilities (especially for vegetables), poor infrastructure and insufficient tillage and field preparation techniques."
  toDiscuss should s"contain one belief in '${sent34}'" in {
    val mentions = getMentions(sent34)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("farmers")
    m.arguments("belief").head.text should be ("low production levels of crops and post-harvest losses originated primarily from lack of well-functioning machinery, storage facilities (especially for vegetables), poor infrastructure")

  }

  val sent35 = "Farmers also explained the ongoing shift of rice cultivation from WS to HDS due to biophysical and environmental constraints."
  toDiscuss should s"contain one belief in '${sent35}'" in {
    val mentions = getMentions(sent35)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Farmers")
    m.arguments("belief").head.text should be ("ongoing shift of rice cultivation from WS to HDS")

  }

  val sent36 = "Despite farmers' willingness to diversify crop production, growing new crops is risky and self-financed."
  passingTest should s"contain one belief in '${sent36}'" in {
    val mentions = getMentions(sent36)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("farmers")
    m.arguments("belief").head.text should be ("diversify crop production")

  }

  val sent37 = "The Kissal Patim manager expressed surprise that the percentage of severely food insecure households in Round 1 (76 percent) was not in fact higher due to the high level of vulnerability in her zone."
  failingTest should s"contain one belief in '${sent37}'" in {
    val mentions = getMentions(sent37)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("Kissal Patim manager")
    m.arguments("belief").head.text should be ("percentage of severely food insecure households in Round 1 (76 percent) was not in fact higher due to the high level of vulnerability in her zone")

  }

  val sent38 = "However, FEPROMAS detected a cultural norm of negative stigma attached with receiving food aid, which may explain the disconnect between the high proportion of members who were experiencing moderate food insecurity but who said they did not need any food aid."
  passingTest should s"contain one belief in '${sent38}'" in {
    val mentions = getMentions(sent38)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("belief").head.text should be ("receiving food aid")

  }

  val sent39 = "most of their members who borrowed did not perceive that the original source of the credit was the banks."
  passingTest should s"contain one belief in '${sent39}'" in {
    val mentions = getMentions(sent39)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("most of their members")
    m.arguments("belief").head.text should be ("original source of the credit was the banks")

  }

  val sent40 = "The networks recognized the importance of maintaining extension services to ensure that their farmer members applied best practices."
  passingTest should s"contain one belief in '${sent40}'" in {
    val mentions = getMentions(sent40)
    mentions should have size(1)

    val m = mentions.head
    m.arguments("believer").head.text should be ("networks")
    m.arguments("belief").head.text should be ("maintaining extension services to ensure that their farmer members applied") // fixme: should incl `best practices`

  }

//  val sent41 = "The focus group results also demonstrated that, despite the pandemic, climate and short-term weather information was considered a priority value-added service for which network members demonstrated a willingness to pay, even during these difficult times."
//  passingTest should s"contain one belief in '${sent41}'" in {
//    val mentions = getMentions(sent41)
//    mentions should have size(1)
//
//    val m = mentions.head
//    m.arguments("believer").head.text should be ("network members")
//    m.arguments("belief").head.text should be ("pay")
//
//  }


    //to check if sentences less than 150 tokens are only let through
    val sent41 = "The New York Times is skeptical of Trump’s claims that Ron Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorism"
  sent41 should "have no beliefs" in {
      val mentions = getMentions(sent41)
      mentions should have size (0)
    }

  //to check if sentences with less than half integer numbers are only let through
  val sent42 = "20 45 Trump 20 45 believes 20 45 all  20 45 are losers"
  sent42 should "have no beliefs" in {
    val mentions = getMentions(sent42)
    mentions should have size (0)
  }

  //to check if sentences with less than half  numbers (float or integers) are only let through
  val sent43 = "20.45 Trump 2.045 believes 20 45 20 45 20 45 20 45 all  20 45 are losers"
  sent43 should "have no beliefs" in {
    val mentions = getMentions(sent43)
    mentions should have size (0)
  }

}