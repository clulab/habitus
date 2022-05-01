package org.clulab.habitus.variables

import org.clulab.habitus.utils.Test
import org.clulab.odin.Mention

class EntitiesTest extends Test {
  // checks entity mentions:
  // the variable argument should be a text bound mention of the correct label
  // the value argument should have the correct expected extraction.
  val vp: VariableProcessor = VariableProcessor()

  case class VariableTest(
                           name: String,
                           // Text for extraction
                           text: String,
                           // Expected label extraction; a text can have multiple extractions for multiple labels.
                           labelExpected: Array[String],
                           // Expected values from text extraction.
                           expectedTextValue: Array[String]
                         ) {

    def getMentions(text: String): Seq[Mention] = {
      val parsingResults = vp.parse(text)
      // in this tester, we want to get all mentions, including entity ones, so use parsingResults.allMentions
      // instead of parsingResults.targetMentions
      parsingResults.allMentions
    }

    def test(index: Int): Unit = {
      it should s"process $index-$name correctly" in {
        if (index == -1)
          println("Put a breakpoint here to observe a particular test.")

        // get all mentions from text
        val mentions = getMentions(text)

        // filter mentions by expected labels
        for (label <- labelExpected){
          val targetMentions =  mentions.filter(_.label matches  label)
          targetMentions should have size (1)
        }
        // I think I would need a better way to map ONLY extracted values. For now, here is what I am able to do.
        // I do not know how to use "append" like in python, I could have append ONLY extracted
        // entities to `targetMentionsLabelTexts` from the `for loop above` and check for their existence in the last line.
        val targetMentionsLabelTexts = mentions.map(_.text)
        val desiredLabelTexts = expectedTextValue
        desiredLabelTexts.foreach(text => targetMentionsLabelTexts should contain(text))

      }
    }
  }

  behavior of "VariableReader Entities"

  val variableTests: Array[VariableTest] = Array(
    VariableTest(
      "sent1",
      "with an average yield over years and seasons of 5 t ha-1",
      Array("Yield"),
      Array("yield")
    ),
    VariableTest(
      "sent2",
      "The potential grain yield that can be obtained ranges from 8 to 9 t ha-1 in the wet season (July sowing) and from 6 to 11 t ha-1 in the dry season (February sowing)",
      Array("Yield","WetSeason", "DrySeason"),
      Array("yield", "wet season", "dry season")
    ),
    VariableTest(
      "sent3",
      "Double cropping (growing rice in the wet season and dry season on the same field) is possible.",
      Array("WetSeason", "DrySeason"),
      Array("wet season", "dry season")
    ),
    VariableTest(
      "sent4",
      "irrigation rules resulted in great variability of irrigation frequency between fields, and sub-optimal timing of nitrogen fertilizer application resulted in yield losses",
      Array("Yield"),
      Array("yield")
    ),



  )


  variableTests.zipWithIndex.foreach { case (variableTest, index) => variableTest.test(index) }
}
