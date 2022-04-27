package org.clulab.habitus.variables

import org.clulab.habitus.utils.Test
import org.clulab.odin.Mention
import org.scalatest.{FlatSpec, Matchers}

class TestVariableReaderEntities extends Test {
  val vp = VariableProcessor()

  def getMentions(text: String): Seq[Mention] = {
    val parsingResults = vp.parse(text)
    // in this tester, we want to get all mentions, including entity ones, so use parsingResults.allMentions
    // instead of parsingResults.targetMentions
    parsingResults.allMentions
  }

  behavior of "VariableReader Entities"

  val sent1 = "The Senegal River Valley, located in Sahel zone, is one of the major irrigated rice-producing areas in Senegal. "
  passingTest should "recognize three locations" in {
    // extractio mentions
    val mentions = getMentions(sent1)
    // get mentions that we are interested in in this test based on the label ("Location")
    val targetMentions = mentions.filter(_.label matches "Location")
    // check the right number of locations for extracted
    targetMentions should  have size (3)
    // we will be checking based on text of mentions, so remap the sequence of mentions to sequence of texts of these mentions
    val targetMentionsTexts = targetMentions.map(_.text)
    // these are the texts we want to extract
    val desired = Seq("Senegal River Valley", "Sahel", "Senegal")
    // make sure all the texts we want to extract exist among the extracted texts
    desired.foreach(d => targetMentionsTexts should contain(d))
  }

  val sent2 = "In all scenarios, Sahel 108 variety is sown."
  passingTest should "recognize a crop variety" in {
    val mentions = getMentions(sent2)
    val targetMentions = mentions.filter(_.label == "Crop")
    targetMentions should have size (1)
    // there is only one crop mention here (which we tested one line above), so we can just
    // check the first (the head) one
    targetMentions.head.text should equal("Sahel 108")
  }

  val sent3 = "These correspond to the dry season (from February/March to June/July)."
  passingTest should "recognize dry season" in {
    val mention = getMentions(sent3)
    val targetMention = mention.filter(_.label matches "DrySeason")
    targetMention should have size(1)
    //todo: write a test here that will make sure that the date range "February/March to June/July" is extracted
    //(Note: since we will construct this range from smaller date text bound mentions, this range does not have to be the only one extracted
    targetMention.head.text should equal ("dry season")
  }

  val sent4 = "Farmers’ yields are on average between 4 and 5 t ha-1, and, therefore, far below potential. "
  passingTest should "recognize a yield" in {
    val mentions = getMentions(sent4)
    val targetMentions = mentions.filter(_.label == "Yield")
    targetMentions should have size (1)
    // there is only one crop mention here (which we tested one line above), so we can just
    // check the first (the head) one
    targetMentions.head.text should equal("yields")
  }

  val sent5 = "The potential grain yield that can be obtained ranges from 8 to 9 t ha-1 in the wet season (July sowing) and from 6 to 11 t ha-1 in the dry season (February sowing)"
  passingTest should "recognize a yield, wet season and dry season" in {
    val mentions = getMentions(sent5)
    val targetLabels = Array("Yield", "WetSeason", "DrySeason")
    for (label <- targetLabels){
      val targetMentions = mentions.filter(_.label ==  label)
      targetMentions should have size (1)
    }
    val targetMentionsLabelTexts = mentions.map(_.text)
    val desiredLabelTexts = Seq("yield", "wet season", "dry season")
    desiredLabelTexts.foreach(text => targetMentionsLabelTexts should contain(text))
  }

  val sent6 = "Double cropping (growing rice in the wet season and dry season on the same field) is possible."
  passingTest should "recognize two seasons" in {
    val mentions = getMentions(sent6)

    val targetLabels = Array("WetSeason", "DrySeason")
    for (label <- targetLabels){
      val targetMentions = mentions.filter(_.label ==  label)
      targetMentions should have size (1)
    }
    val targetMentionsLabelTexts = mentions.map(_.text)
    val desiredLabelTexts = Seq("wet season", "dry season")
    desiredLabelTexts.foreach(text => targetMentionsLabelTexts should contain(text))
  }

  val sent7 = "irrigation rules resulted in great variability of irrigation frequency between fields, and sub-optimal timing of nitrogen fertilizer application resulted in yield losses"
  passingTest should "identify yield" in {
    val mentions = getMentions(sent7)
    val targetMention = mentions.filter(_.label == "Yield")
    //check the size of the mention
    targetMention should have size(1)
    // confirm extraction
    targetMention.head.text should equal("yield")
  }

  val sent8 = "The potential yields of these three cultivars are similar and are on average about 8 to 9 t ha-1 in the wet season"
  passingTest should "recognize yields and wet season" in {
    val mentions = getMentions(sent8)

    val targetLabels = Array("Yield", "WetSeason")
    for (label <- targetLabels) {
      val targetMentions = mentions.filter(_.label == label)
      targetMentions should have size (1)
    }
    val targetMentionsLabelTexts = mentions.map(_.text)
    val desiredLabelTexts = Seq("yields", "wet season")
    desiredLabelTexts.foreach(text => targetMentionsLabelTexts should contain(text))
  }

  val sent9 = "in the 1999WS, with an average grain yield of 7.2 t ha–1. In the 2000WS"
  passingTest should "find yield" in {
    val mentions = getMentions(sent9)
    val targetMention = mentions.filter(_.label matches "Yield")
    targetMention should have size(1)
    targetMention.head.text should equal("yield")
  }

  val sent10 = "the average grain yield was 8.2 t ha–1"
  passingTest should "be able to find yield" in {
    val mentions = getMentions(sent10)
    val targetMention = mentions.filter(_.label matches "Yield")
    targetMention should have size(1)
    targetMention.head.text should equal("yield")
  }
  val sent11 = "irrigation rules resulted in great variability of irrigation frequency between fields, and sub-optimal timing of nitrogen fertilizer application resulted in yield losses"
  passingTest should "be able identify yield" in {
    val mentions = getMentions(sent11)
    val targetMention = mentions.filter(_.label matches "Yield")
    targetMention should have size(1)
    targetMention.head.text should equal("yield")
  }
  val sent12 = "Average yield reached 7.2 t ha–1 in 1999 and 8.2 t ha–1 in 2000"
  passingTest should "locate yield" in {
    val mentions = getMentions(sent12)
    val targetMention = mentions.filter(_.label matches "Yield")
    targetMention should have size(1)
    targetMention.head.text should equal("yield")
  }
  val sent13 = "Potential rice grain yields (limited by solar radiation and temperature only) are on average about 9 t ha–1 in the wet growing season from July to November"
  passingTest should "locate yield" in {
    val mentions = getMentions(sent13)
    val targetMention = mentions.filter(_.label matches "Yield")
    targetMention should have size (1)
    targetMention.head.text should equal("yield")
  }
}
