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
  ignore should "recognize a complex date range" in {
    //todo: write a test here that will make sure that the date range "February/March to June/July" is extracted
    //(Note: since we will construct this range from smaller date text bound mentions, this range does not have to be the only one extracted
  }

}
