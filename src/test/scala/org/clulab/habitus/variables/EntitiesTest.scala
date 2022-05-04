package org.clulab.habitus.variables

import org.clulab.habitus.utils.Test
import org.clulab.odin.Mention

import scala.collection.mutable.ArrayBuffer

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
                           desired:  Seq[(String, Seq[String])]
                           // Expected values from text extraction.
                         ) {

    def getMentions(text: String): Seq[Mention] = {
      val parsingResults = vp.parse(text)
      // in this tester, we want to get all mentions, including entity ones, so use parsingResults.allMentions
      // instead of parsingResults.targetMentions
      parsingResults.allMentions
    }




    def test(index: Int): Unit = {
      it should s"filter by correct label(s) and value size of sent $index-$name correctly" in {
        if (index == -1)
          println("Put a breakpoint here to observe a particular test.")

        // get all mentions from text
        val mentions = getMentions(text)

        // iterate over label, value pairs
        for ((label, values) <- desired){

          // filter mentions by label
          val targetMentions = mentions.filter(_.label == label)

          //check size of filtered mentions with the values
          targetMentions.length should equal(values.length)


          val targetMentionTexts = targetMentions.map(_.text)
          println(targetMentionTexts)

          //check if the required texts were extracted
          values.foreach(text => targetMentionTexts should contain(text))

        }

      }
    }
  }

  behavior of "VariableReader Entities"

  val variableTests: Array[VariableTest] = Array(
          VariableTest(
            "sent1",
            "with an average yield over years yields and seasons of 5 t ha-1",
            Seq("Yield" -> Seq("yield", "yields"),
                "Value" -> Seq("5 t ha-1"))
          ),
          VariableTest(
            "sent2",
            "The potential grain yield that can be obtained ranges from 8 to 9 t ha-1 in the wet season (July sowing) and from 6 to 11 t ha-1 in the dry season (February sowing)",
            Seq("Yield" -> Seq("yield"),
              "Value" -> Seq("from 8 to 9 t ha-1", "from 6 to 11 t ha-1", "July", "February"),
              "WetSeason"-> Seq("wet season"),
              "DrySeason"-> Seq("dry season"),
              "Variable"-> Seq("sowing", "sowing"),
              "Date"-> Seq("July", "February")
            )
          ),
          VariableTest(
            "sent3",
            "Double cropping (growing rice in the wet season and dry season on the same field) is possible.",
            Seq(
              "Crop" -> Seq("rice"), // rice extracted both as `Crop` and
              "Value" -> Seq("rice"), // `Value` value.
              "WetSeason"-> Seq("wet season"),
              "DrySeason"-> Seq("dry season"),
              "Variable"-> Seq("growing", "cropping")
            )
          ),
          VariableTest(
            "sent4",
            "irrigation rules resulted in great variability of irrigation frequency between fields, and sub-optimal timing of nitrogen fertilizer application resulted in yield losses",
            Seq("Yield" -> Seq("yield"),
              "Value" -> Seq("from 8 to 9 t ha-1", "from 6 to 11 t ha-1", "July", "February"),
              "WetSeason"-> Seq("wet season"),
              "DrySeason"-> Seq("dry season"),
              "Variable"-> Seq("fertilizer application", "sowing"),
              "Date"-> Seq("July", "February")
            )
          ),
//          VariableTest(
//            "sent5",
//            "The potential yields of these three cultivars are similar and are on average about 8 to 9 t ha-1 in the wet season",
//            Array("Value","Variable", "WetSeason", "Yield"),
//            Array(1, 1, 1, 1)
//          ),
//          VariableTest(
//            "sent6",
//            "in the 1999WS, with an average grain yield of 7.2 t ha–1. In the 2000WS",
//            Array("Value", "Yield"),
//            Array(1, 1)
//          ),
//          VariableTest(
//            "sent7",
//            "the average grain yield was 8.2 t ha–1",
//            Array("Value", "Yield"),
//            Array(1, 1)
//          ),
//          VariableTest(
//            "sent8",
//            "Average yield reached 7.2 t ha–1 in 1999 and 8.2 t ha–1 in 2000",
//            Array("Value", "Yield"),
//            Array(4, 1)
//          ),
//          VariableTest(
//            "sent9",
//            "Potential rice grain yields (limited by solar radiation and temperature only) are on average about 9 t ha–1 in the wet growing season from July to November",
//            Array("Value", "Yield", "Variable", "WetSeason"),
//            Array(3, 1, 1, 1)
//          ),
//          VariableTest(
//            "sent10",
//            "The Senegal River Valley, located in Sahel zone, is one of the major irrigated rice-producing areas in Senegal.",
//            Array("Location"),
//            Array(3)
//          ),
//          VariableTest(
//            "sent11",
//            "In all scenarios, Sahel 108 variety is sown.",
//            Array("Value", "Variable"),
//            Array(1, 2)
//          ),
//          VariableTest(
//            "sent12",
//            "These correspond to the dry season (from February/March to June/July).",
//            Array("Value", "Date", "DrySeason"),
//            Array(5, 1, 1)
//          ),
//          VariableTest(
//            "sent13",
//            "Farmers’ yields are on average between 4 and 5 t ha-1, and, therefore, far below potential.",
//            Array("Value", "Yield"),
//            Array(1, 1)
//          ),


  )


  variableTests.zipWithIndex.foreach { case (variableTest, index) => variableTest.test(index) }
}
