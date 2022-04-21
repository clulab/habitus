package org.clulab.habitus.beliefs

import org.clulab.habitus.utils.Test
import org.clulab.odin.Mention

class TestBeliefReader extends Test {
  val FAIL = 0
  val PASS = 1
  val IGNORE = 2
  val DISCUSS = 3
  val BROKEN_SYNTAX = 4
  val COMMENTED_OUT = 5

  val bp: BeliefProcessor = BeliefProcessor()

  // (believerText or beliefThemeText, beliefText)
  type Belief = (String, String)

  case class BaseBeliefTest(
    mode: Int, name: String, text: String,
    beliefs: Seq[Belief], believerKey: String
  ) {

    def getMentions(text: String): Seq[Mention] = {
      val parsingResults = bp.parse(text)
      // Check all labels for Belief to account for LikelyFact beliefs.
      parsingResults.targetMentions.filter(_.labels contains "Belief")
    }

    def innerTest(index: Int): Unit = {
      if (index == -1)
        println("Put a breakpoint here to observe a particular test.")

      val mentions = getMentions(text)
      mentions should have size beliefs.length

      mentions.zip(beliefs).zipWithIndex.foreach { case ((mention, belief), beliefIndex) =>
        if (belief._1.nonEmpty) {
          val believerMentions = mention.arguments(believerKey)
          (beliefIndex, believerMentions.size) should be (beliefIndex, 1)
          (beliefIndex, believerMentions.head.text) should be ((beliefIndex, belief._1))
        }

        val beliefMentions = mention.arguments("belief")
        (beliefIndex, beliefMentions.size) should be ((beliefIndex, 1))
        (beliefIndex, beliefMentions.head.text) should be ((beliefIndex, belief._2))
      }
    }

    def test(index: Int): Unit = {
      val title = s"process $index-$name correctly"

      mode match {
        case FAIL => failingTest should title in innerTest(index)
        case PASS => passingTest should title in innerTest(index)
        case IGNORE => ignore should title in innerTest(index)
        case DISCUSS => toDiscuss should title in innerTest(index)
        case BROKEN_SYNTAX => brokenSyntaxTest should title in innerTest(index)
        case COMMENTED_OUT =>
        case _ => it should title in {
          throw new RuntimeException(s"The mode '$mode' is unrecognized.")
        }
      }
    }
  }

  class BeliefTest(mode: Int, name: String, text: String, beliefs: Seq[Belief])
      extends BaseBeliefTest(mode, name, text, beliefs, "believer")

  object BeliefTest {
    def apply(mode: Int, name: String, text: String, beliefs: Seq[Belief]): BeliefTest =
      new BeliefTest(mode, name, text, beliefs)
  }

  class BeliefThemeTest(mode: Int, name: String, text: String, beliefs: Seq[Belief])
      extends BaseBeliefTest(mode, name, text, beliefs, "beliefTheme")

  object BeliefThemeTest {
    def apply(mode: Int, name: String, text: String, beliefs: Seq[Belief]): BeliefThemeTest =
      new BeliefThemeTest(mode, name, text, beliefs)
  }

  val beliefTests: Array[BaseBeliefTest] = Array(
    BeliefTest(
      PASS, "sent1", "farmers believe that loans are useful.",
      Seq(("farmers", "loans are useful"))
    ),
    BeliefTest(
      PASS, "sent2", "I believe Trump",
      // We do not allow beliefs that are not propositions.
      // Seq(("I", "Trump"))
      Seq.empty
    ),
    BeliefTest(
      PASS, "sent3", "Most women think Trump is a bad person",
      Seq(("Most women", "Trump is a bad person"))
    ),
    BeliefTest(
      DISCUSS, "sent4", "Allegra accepts that Paul makes good beer",
      Seq(("Allegra", "Paul makes good beer"))
    ),
    BeliefTest(
      DISCUSS, "sent5", "Allegra considers that Dogfish Head makes better beer",
      Seq(("Allegra", "Dogfish Head makes better beer"))
    ),
    BeliefTest(
      DISCUSS, "sent6", "Allegra concludes that Paul makes the best beer",
      Seq(("Allegra", "Paul makes the best beer"))
    ),
    BeliefTest(
      PASS, "sent7", "Daniel doubts that there will be more virulent mutations",
      Seq(("Daniel", "there will be more virulent mutations"))
    ),
    BeliefTest(
      DISCUSS, "sent8", "I reject the premises of Social Darwinism",
      Seq(("I", "premises of Social Darwinism"))
    ),
    BeliefTest(
      PASS, "sent9", "I suppose that Tröegs might know a thing or two about beer",
      Seq(("I", "Tröegs might know a thing or two about beer"))
    ),
    BeliefTest(
      // Tests sent10 - sent_10_10 are author beliefs, which we are not extracting now.
      IGNORE, "sent10", "It's credible that Rudy Giuliani broke the law",
      Seq(("It", "Rudy Giuliani broke the law"))
    ),
    BeliefTest(
      IGNORE, "sent10_0", "It's not credible that Rudy Giuliani broke the law",
      Seq(("It", "Rudy Giuliani broke the law"))
    ),
    BeliefTest(
      IGNORE, "sent10_1", "It's convincing that Rudy Giuliani broke the law",
      Seq(("It", "Rudy Giuliani broke the law"))
    ),
    BeliefTest(
      IGNORE, "sent10_2", "It's believable that Rudy Giuliani broke the law",
      Seq(("It", "Rudy Giuliani broke the law"))
    ),
    BeliefTest(
      IGNORE, "sent10_3", "It's inconceivable that anyone would hate The Princess Bride",
      Seq(("It", "anyone would hate The Princess Bride"))
    ),
    BeliefTest(
      IGNORE, "sent10_4", "It's suspect that Rudy Giuliani broke the law",
      Seq(("It", "Rudy Giuliani broke the law"))
    ),
    BeliefTest(
      IGNORE, "sent10_5", "It's plausible that Rudy Giuliani broke the law",
      Seq(("It", "Rudy Giuliani broke the law"))
    ),
    BeliefTest(
      IGNORE, "sent10_6", "It's questionable that anyone would hate The Princess Bride",
      Seq(("It", "anyone would hate The Princess Bride"))
    ),
    BeliefTest(
      IGNORE, "sent10_7",  "It's dubious that anyone would hate The Princess Bride",
      Seq(("It", "anyone would hate The Princess Bride"))
    ),
    BeliefTest(
      IGNORE, "sent10_8", "It's equivocal that anyone would hate The Princess Bride",
      Seq(("It", "anyone would hate The Princess Bride"))
    ),
    BeliefTest(
      IGNORE, "sent10_9", "It's conclusive that anyone would hate The Princess Bride",
      Seq(("It", "anyone would hate The Princess Bride"))
    ),
    BeliefTest(
      IGNORE, "sent10_10", "It's false that anyone would hate The Princess Bride",
      Seq(("It", "anyone would hate The Princess Bride"))
    ),
    BeliefTest(
      DISCUSS, "sent11", "The New York Times is skeptical of Trump’s claims",
      Seq(("New York Times", "Trump’s claims"))
    ),
    BeliefTest(
      // fixme: expand on nmod_than
      FAIL, "sent13", "There’s no reason to think that Mu is worse than Delta",
      Seq(("", "Mu is worse than Delta"))
    ),
    BeliefTest(
      DISCUSS, "sent14_1", "Violent storms are consistent with climate change",
      Seq(("Violent storms", "climate change"))
    ),
    BeliefTest(
      DISCUSS, "sent14_2", "Violent storms are explained by climate change",
      Seq(("Violent storms", "climate change"))
    ),
    BeliefTest(
      DISCUSS, "sent14_3", "Violent storms are predicted by climate change",
      Seq(("Violent storms", "climate change"))
    ),
    BeliefTest(
      IGNORE, "sent14_4", "It's hard to see how burning coal helps the environment",
      Seq(("It", "how burning coal helps the environment"))
    ),
    BeliefThemeTest( // Note!
      PASS, "sent15", "Trump is thought to be a narcissist",
      Seq(("Trump", "narcissist"))
    ),
    BeliefTest(
      PASS, "sent16", "Paul is sure that bleach kills covid.",
      Seq(("Paul", "bleach kills covid"))
    ),
    BeliefTest(
      // fixme: full mention span does not include `sterility`
      PASS, "sent17", "Paul is unsure about whether covid vaccination causes sterility.",
      Seq(("Paul", "covid vaccination causes sterility"))
    ),
    BeliefTest(
      BROKEN_SYNTAX, "sent18", "Paul questions the popular theory that Trump is an idiot.",
      Seq(("Paul", "Trump is an idiot"))
    ),
    BeliefTest(
      // TODO: Returns many believes associations
      FAIL, "sent19", "Senator Harris mistrusts Trump’s medical recommendations but trusts Fauci’s",
      Seq(("Senator Harris", "Trump’s medical recommendations"))
      // Mistrust is particularly high in Senegal
      //(83a) and Liberia (78P‹).}
    ),
    BeliefTest(
      FAIL, "sent20", "Only three in 10 respondents (31a) say they trust their government somewhat or a lot to ensure that any vaccine is safe before it is offered to citizens..",
      Seq(("three in 10 respondents", "their government somewhat or a lot to ensure that any vaccine is safe"))
    ),
    BeliefTest(
      FAIL, "sent21", "I expect my investments to do well",
      Seq(("I", "my investments to do well"))
    ),
    BeliefTest(
      DISCUSS, "sent22", "I look forward to my investments doing well",
      Seq(("I", "my investments doing well"))
    ),
    BeliefTest(
      FAIL, "sent23", "We are all worried about global warming.",
      Seq(("We", "global warming"))
    ),
    BeliefTest(
      DISCUSS, "sent24", "The IPCC is pessimistic about countries meeting their Paris targets.",
      Seq(("IPCC", "countries meeting their Paris targets"))
    ),
    BeliefTest(
      DISCUSS, "sent25", "We fear another Trump presidency.",
      Seq(("We", "Trump presidency"))
    ),
    BeliefTest(
      PASS, "sent26", "Paul is hopeful that Breyer will step down.",
      Seq(("Paul", "Breyer will step"))
      // fixme: should incl `down`
    ),
    BeliefTest(
      PASS, "sent27", "We are optimistic about the future of this research.",
      Seq(("We", "future of this research"))
    ),
    BeliefTest(
      DISCUSS, "sent28", "The Government of Senegal and other key stakeholders acknowledge that a major issue in the country is that there is a lack of effective concerted planning of the climate change efforts.",
      Seq(("Government of Senegal and other key stakeholders", "major issue in the country is that there is a lack of effective concerted planning"))
    ),
    BeliefTest(
      COMMENTED_OUT,"sent29", "the Government of Senegal is giving highest priority to rice self-sufficiency",
      Seq(("Government of Senegal", "rice self-sufficiency"))
    ),
    BeliefTest(
      DISCUSS, "sent30", "Although progress in the art and science of seasonal forecasting and other climate information services is being made, ANACIM acknowledges that the approximately 70% accuracy of its seasonal forecasts leave considerable room for improvement.",
      Seq(("ANACIM", "70% accuracy of its seasonal forecasts leave considerable room for improvement"))
    ),
    BeliefTest(
      DISCUSS, "sent31", "It is very positive that the Mission has already begun to identify and fill some initial critical gaps related to the context-specific climate change risks in Senegal in its Feed the Future portfolio",
      Seq(("", "Mission has already begun to identify and fill some initial critical gaps related to the context-specific climate change risks"))
    ),
    BeliefTest(
      // This is probably preference.
      COMMENTED_OUT, "sent32", "Most interviewed farmers expressed the desire and ambition to free up time to participate in family and community events.",
      Seq(("interviewed farmers", "desire and ambition to free up time to participate in family and community events"))
    ),
    BeliefTest(
      DISCUSS, "sent33", "Farmers perceived that rice intensification was driven by political and financial incentives",
      Seq(("Farmers", "rice intensification was driven by political and financial incentives"))
    ),
    BeliefTest(
      DISCUSS, "sent34", "The farmers stated that the low production levels of crops and post-harvest losses originated primarily from lack of well-functioning machinery, storage facilities (especially for vegetables), poor infrastructure and insufficient tillage and field preparation techniques.",
      Seq(("farmers", "low production levels of crops and post-harvest losses originated primarily from lack of well-functioning machinery, storage facilities (especially for vegetables), poor infrastructure"))
    ),
    BeliefTest(
      DISCUSS, "sent35", "Farmers also explained the ongoing shift of rice cultivation from WS to HDS due to biophysical and environmental constraints.",
      Seq(("Farmers", "ongoing shift of rice cultivation from WS to HDS"))
    ),
    BeliefTest(
      PASS, "sent36", "Despite farmers' willingness to diversify crop production, growing new crops is risky and self-financed.",
      Seq(("farmers", "diversify crop production"))
    ),
    BeliefTest(
      FAIL, "sent37", "The Kissal Patim manager expressed surprise that the percentage of severely food insecure households in Round 1 (76 percent) was not in fact higher due to the high level of vulnerability in her zone.",
      Seq(("Kissal Patim manager", "percentage of severely food insecure households in Round 1 (76 percent) was not in fact higher due to the high level of vulnerability in her zone"))
    ),
    BeliefTest(
      PASS, "sent38", "However, FEPROMAS detected a cultural norm of negative stigma attached with receiving food aid, which may explain the disconnect between the high proportion of members who were experiencing moderate food insecurity but who said they did not need any food aid.",
      Seq(("", "receiving food aid"))
    ),
    BeliefTest(
      PASS, "sent39", "most of their members who borrowed did not perceive that the original source of the credit was the banks.",
      Seq(("most of their members", "original source of the credit was the banks"))
    ),
    BeliefTest(
      PASS, "sent40", "The networks recognized the importance of maintaining extension services to ensure that their farmer members applied best practices.",
      Seq(("networks", "maintaining extension services to ensure that their farmer members applied"))
      // fixme: should incl `best practices`
    ),
    BeliefTest(
      COMMENTED_OUT, "sent41", "The focus group results also demonstrated that, despite the pandemic, climate and short-term weather information was considered a priority value-added service for which network members demonstrated a willingness to pay, even during these difficult times.",
      Seq(("network members", "pay"))
    ),
    BeliefTest(
      // Check if sentences less than 150 tokens are only let through.
      PASS, "sent41", "The New York Times is skeptical of Trump’s claims that Ron Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorismRon Johnson views demonstrations about the George Floyd killing as terrorism",
      Seq.empty
    ),
    BeliefTest(
      // Check if sentences with less than half integer numbers are only let through.
      PASS, "sent42", "20 45 Trump 20 45 believes 20 45 all  20 45 are losers",
      Seq.empty
    ),
    BeliefTest(
      // Check if sentences with less than half numbers (float or integers) are only let through.
      PASS, "sent43", "20.45 Trump 2.045 believes 20 45 20 45 20 45 20 45 all  20 45 are losers",
      Seq.empty
    )
  )

  beliefTests.zipWithIndex.foreach { case (beliefTest, index) => beliefTest.test(index) }
}
