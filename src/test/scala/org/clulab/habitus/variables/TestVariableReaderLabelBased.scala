package org.clulab.habitus.variables

import org.clulab.habitus.utils.Test
import org.clulab.odin.{Mention, TextBoundMention}

class TestVariableReaderLabelBased extends Test {
  // checks Assignment mentions:
  // the variable argument should be a text bound mention of the correct label
  // the value argument should have a correct norm
  val vp: VariableProcessor = VariableProcessor()

  // (variableLabel, Seq[(valueText, valueNorm)])
  // So if one mention has multiple values, write Seq((valueText1, valueNorm1), (valueText2, valueNorm2), ...)
  type Variable = (String, Seq[(String, String)])

  case class VariableTest(
     name: String, text: String,
     // If there are multiple "Assignment" mentions, use one variable for each and multiple lines.
     label: String,
     variables: Seq[Variable]
   ) {

    def getMentions(text: String): Seq[Mention] = {
      val parsingResults = vp.parse(text)
      parsingResults.allMentions
    }

    def test(index: Int): Unit = {
      it should s"process $index-$name correctly" in {
        if (index == -1)
          println("Put a breakpoint here to observe a particular test.")

        val allMentions = getMentions(text)
        val mentions = allMentions.filter(_.label matches label).sortBy(_.tokenInterval)

        if (variables.isEmpty) {
          // get only relations and events
          val nonTextBoundMentions = mentions.filterNot(m => m.isInstanceOf[TextBoundMention])
          // there should be none
          nonTextBoundMentions.length should be (0)
        } else {

          mentions should have size variables.length

          variables.zip(mentions).zipWithIndex.foreach { case ((variable, mention), variableIndex) =>
            (variableIndex, mention.arguments("variable").head.label) should be((variableIndex, variable._1))

            val values = variable._2
            val arguments = mention.arguments("value")

            arguments should have size values.length
            values.zip(arguments).zipWithIndex.foreach { case ((value, argument), valueIndex) =>
              // not checking text right now, just the norm
              //            (variableIndex, valueIndex, argument.text) should be ((variableIndex, valueIndex, value._1))
              (variableIndex, valueIndex, argument.norms.get.head) should be((variableIndex, valueIndex, value._2))
            }
          }
        }
      }
    }
  }

  behavior of "VariableReader"

  val variableTests: Array[VariableTest] = Array(
    VariableTest(
      "sent1", "with an average yield over years and seasons of 5 t ha-1",
      "YieldAmount",
      Seq(
        ("Quantity", Seq(("5 t ha-1", "5.0 t/ha"))) //FIXME, should the assignment for yield then be attachments from `AreaSize`?
      )
    ),
    VariableTest(
      "sent2", "Farmers’ yields are on average between 4 and 5 t ha-1, and, therefore, far below potential yields.",
      "YieldAmount",
      Seq(
        ("Yield", Seq(("between 4 and 5 t ha-1", "4.0 -- 5.0 t/ha"))) //FIXME same here.
      )
    ),
    VariableTest(
      "sent3", "the average grain yield was 8.2 t ha–1",
      "YieldAmount",
      Seq(
        ("Yield", Seq(("8.2 t ha–1", "8.2 t/ha"))) //FIXME same here
      )
    ),
    VariableTest(
      "sent4", "in the 1999WS, with an average grain yield of 7.2 t ha–1. In the 2000WS",
      "YieldAmount",
      Seq(
        ("Yield", Seq(("7.2 t ha–1", "7.2 t/ha"))) //FIXME same here
      )
    ),
//    VariableTest(
//      "sent5", "These correspond to the dry season (from February/March to June/July)",
//      Seq(
//        ("DrySeason", Seq(("July", "XXXX-07-XX")))
//      )
//    )
    VariableTest(
      "sent5", "Potential rice grain yields (limited by solar radiation and temperature only) are on average about 9 t ha–1 in the wet growing season from July to November",
      "YieldAmount",
      Seq(
        ("Yield", Seq(("9 t ha–1", "9.0 t/ha"))) //FIXME same here
      )
    ),
    VariableTest(
      "sent5_1", "Potential rice grain yields (limited by solar radiation and temperature only) are on average about 9 t ha–1 in the wet growing season from July to November",
      "WetSeasonAssignment",
      Seq(
        ("WetSeason", Seq(("from July to November", "XXXX-07-XX -- XXXX-11-XX")))
      )
    ),
    VariableTest(
      "sent6", "actual average farmer yields are about 5 t ha–1 ",
      "YieldAmount",
      Seq(
        ("Yield", Seq(("5 t ha–1", "5.0 t/ha"))) //FIXME same here
      )
    ),
    VariableTest(
      "sent7", "The potential yields of these three cultivars are similar and are on average about 8 to 9 t ha-1 in the wet season",
      "YieldAmount",
      Seq(
        ("Yield", Seq(("5 t ha–1", "8.0 -- 9.0 t/ha"))) //FIXME same here
      )
    ),
    VariableTest(
      "sent8", "The potential grain yield that can be obtained ranges from 8 to 9 t ha-1 in the wet season (July sowing) and from 6 to 11 t ha-1 in the dry season (February sowing)",
      "YieldAmount",
      Seq(
        ("Yield", Seq(("8 to 9 t ha-1", "8.0 -- 9.0 t/ha"), ("6 to 11 t ha-1", "6.0 -- 11.0 t/ha"))) //FIXME same here
      )
    ),
    VariableTest(
      "sent8_1", "The potential grain yield that can be obtained ranges from 8 to 9 t ha-1 in the wet season (July sowing) and from 6 to 11 t ha-1 in the dry season (February sowing)",
      "WetSeasonAssignment",
      Seq(
        ("WetSeason", Seq(("July", "XXXX-07-XX"))),
      )
    ),
    VariableTest(
      "sent8_2", "The potential grain yield that can be obtained ranges from 8 to 9 t ha-1 in the wet season (July sowing) and from 6 to 11 t ha-1 in the dry season (February sowing)",
      "DrySeasonAssignment",
      Seq(
        ("DrySeason", Seq(("February", "XXXX-02-XX"))) //FIXME same here
      )
    ),
    VariableTest(
      "sent9", "The first rice crop is sown during the hot dry season (mostly in February) and the second crop during the wet season (mostly in July).",
      "DrySeasonAssignment",
      Seq(
        ("DrySeason", Seq(("February", "XXXX-02-XX")))
      )
    ),
    VariableTest(
      "sent9_1", "The first rice crop is sown during the hot dry season (mostly in February) and the second crop during the wet season (mostly in July).",
      "WetSeasonAssignment",
      Seq(
        ("WetSeason", Seq(("July", "XXXX-07-XX")))
      )
    ),
    VariableTest(
      "sent10", "The average total amount of N applied was 141 kg ha-1",
      "FertilizerQuantity",
      Seq(
        ("Fertilizer", Seq(("141 kg ha-1", "141.0 kg/ha"))),
      )
    ),
    VariableTest(
      "sent11", "Average yield was 4.8 t ha-1; ", //FIXME same here
      "YieldAmount",
      Seq(
        ("Yield", Seq((" 4.8 t", "4.8 t/ha"))),
      )
    ),
    VariableTest(
      "sent12", "Average yield reached 7.2 t ha-1 in 1999 and 8.2 t ha-1 in 2000", //FIXME same here
      "YieldAmount",
      Seq(
        ("Yield", Seq(("7.2 t ha-1", "7.2 t/ha"), ("8.2 t ha-1", "8.2 t/ha")))
      )
    ),
    VariableTest(
      "sent13", "in the 1999WS, with an average grain yield of 7.2 t ha-1. In the 2000WS", //FIXME same here
      "YieldAmount",
      Seq(
        ("Yield", Seq(("7.2 t ha-1", "7.2 t/ha")))
      )
    ),
    VariableTest(
      "sent14", "and potential yield was taken as 8 t/ha for both seasons in the middle valley", //FIXME same here
      "YieldAmount",
      Seq(
        ("Yield", Seq(("8 t/ha", "8.0 t/ha")))
      )
    ),
    VariableTest(
      "sent15", "Seeding dates ranged from 22 August to 26 September in 2011WS, from 29 February to 1 April in the 2012DS, and from 5 to 23 March in the 2013DS",
      "PlantingDate",
      Seq(
        ("Planting", Seq(("from 22 August to 26 September", "XXXX-08-22 -- XXXX-09-26"))),
        ("Planting", Seq(("from 29 February to 1 April", "XXXX-02-29 -- XXXX-04-01"))),
        ("Planting", Seq(("from 5 to 23 March", "XXXX-03-05 -- XXXX-03-23")))
      )
    ),
    VariableTest(
      "sent16", "WS sowing in July and about 9–10 t ha−1 for dry season (DS) sowing in February in the Senegal River delta.",
      "PlantingDate",
      Seq(
        ("Planting", Seq(("July", "XXXX-07-XX"))),
        ("Planting", Seq(("February", "XXXX-02-XX")))
      )
    ),
    VariableTest(
      "sent17", "Average WS T0 yield was high, i.e. 7.3 ha-1 (ranging from 5.0 to 9.4 t ha-1)", //FIXME same here
      "YieldAmount",
      Seq(
        ("Yield", Seq(("7.3 ha", "7.3 ha"))),
        ("Yield",  Seq(("from 5.0 to 9.4 t", "5.0 -- 9.4 t")))
      )
    ),
    VariableTest(
      "sent18", "considering the region’s potential yield of about 9 t ha-1.", //FIXME same here
      "YieldAmount",
      Seq(
        ("Yield", Seq(("9 t", "9.0 t"))),
      )
    ),
    VariableTest(
      "sent19", "Average DS T0 yield was relatively low, i.e. 4.4 t ha-1 (ranging from 2.5 to 6.0 t ha-1)", //FIXME same here
      "YieldAmount",
      Seq(
        ("Yield", Seq(("4.4 t", "4.4 t"))),
        ("Yield", Seq(("from 2.5 to 6.0 t", "2.5 -- 6.0 t")))
      )
    ),
    VariableTest(
      "sent20", "Average yields in SRV theoretically range between 5.0 and 6.0 t ha-1 in the rainy season and between 6.5 and 7.5 t ha-1 in the dry season",
      "YieldAmount",
      Seq(
        ("Yield", Seq(("between 5.0 and 6.0 t ha-1", "5.0 -- 6.0 t/ha"), ("between 6.5 and 7.5 t ha-1", "6.5 -- 7.5 t/ha"))), //FIXME same here
      )
    ),
    VariableTest(
      "sent21", "seeds of the rice variety Sahel 108 are sown, a short- cycle variety (around 125 days) ",
      "CropAssignment",
      Seq(
        ("GenericCrop", Seq(("Sahel 108", ""))),
      )
    ),
    VariableTest(
      "sent22", "average yields for the two seasons assessed of 4832 kg ha-1 and 7425 kg ha-1 for the areas under CONV and INT management, respectively.",
      "YieldAmount",
      Seq(
        ("Yield", Seq(("4832 kg", "4832.0 kg"), ("7425 kg", "7425.0 kg"))) //FIXME same here
      )
    ),
    VariableTest(
      "negTestSent", "This is a sample negative test. There should be no assignments extracted",
      "None",
      Seq.empty
    ),
    VariableTest(
      "sent23", "timing of basal fertilizer application was on average 26, 33, and 26 days after sowing ( DAS ) in 2011WS, 2012DS, and 2013DS,",
      "FertilizerAssignment",
      Seq(
        ("FertilizerAssignment", Seq(("fertilizer application", "")))
        )
      ),
    VariableTest(
      "sent24", "Rice yields were 6.4, 6.4, and 5.1 t/ha in 2011WS, 2012DS, and 2013DS, respectively",
      "YieldAmount",
      Seq(
        ("Yield", Seq(("5.1 t/ha", "5.1 t/ha"))) // FIXME same here?
      )
    ),
    VariableTest(
      "sent25", "Most farmers in the Senegal River valley apply N fertilizer only twice, i.e. roughly at the start of tillering and panicle initiation (PI).",
      "FertilizerAssignment",
      Seq(
        ("FertilizerAssignment", Seq(("N fertilizer", "")))
      )
    ),
    VariableTest(
      "sent26", "Farmers applied N fertilizer twice, i.e. at the start of tillering and at PI,",
      "FertilizerAssignment",
      Seq(
        ("FertilizerAssignment", Seq(("N fertilizer", "")))
      )
    ),
      VariableTest(
      "sent28", "Average yields in SRV theoretically range between 5.0 and 6.0 t ha-1 in the rainy season and between 6.5 and 7.5 t ha-1 in the dry season (SAED, 2019; USDA-GAIN, 2021)",
      "DrySeasonAssignment",
      Seq(
        ("DrySeason", Seq(("2019", "2019-XX-XX")))
      )
    ),
    VariableTest(
      "sent28_1", "Average yields in SRV theoretically range between 5.0 and 6.0 t ha-1 in the rainy season and between 6.5 and 7.5 t ha-1 in the dry season (SAED, 2019; USDA-GAIN, 2021)",
      "YieldAmount",
      Seq(
        ("Yield", Seq(("between 5.0 and 6.0 t ha-1", "5.0 -- 6.0 t"), ("between 6.5 and 7.5 t ha-1", "6.5 -- 7.5 t"))) //FIXME same here?
      )
    ),
    VariableTest(
      "sent29", "In plots receiving fertilizer, DAP was applied basally (19.3 and 21.5 kg N and P ha−1 ),",
      "FertilizerAssignment",
      Seq(
        ("GenericFertilizer", Seq(("fertilizer, DAP", "")))
      )
    ),
    VariableTest(
      "sent29_2", "In plots receiving fertilizer, DAP was applied basally (19.3 and 21.5 kg N and P ha-1 ),",
      "FertilizerQuantity",
      Seq(
        ("Fertilizer", Seq(("21.5 kg", "21.5 kg")))
      )
    ),
    VariableTest(
      "sent30", "Transplanting for both management systems took place on 7 (2007 and 2008 wet season) and 25 (2009 wet season) August and on 19 March (2008 and 2009 dry season).",
      "WetSeasonAssignment",
      Seq(
        ("WetSeason", Seq(("2009", "2009-XX-XX"))),
        ("WetSeason", Seq(("August", "XXXX-08-XX")))
      )
    ),
    VariableTest(
      "sent21_11", "28,223 ha vs 35,065 ha were used as sown areas.",
      "PlantingArea",
      Seq(("Area", Seq(("28,223 ha", "28223.0 ha"))))
      //note: currently not extracting values after vs.
    ),
    VariableTest(
      "sent21_12", "The areas sown for this 2021/2022 wintering campaign are 28,223 ha vs 35,065 ha in wintering.",
      "PlantingArea",
      Seq(("Area", Seq(("28,223 ha", "28223.0 ha"))))
      //note: currently not extracting values after vs.
    ),
    VariableTest(
      "sent21_13", "Harvests have started in some production areas of the valley, to date an area estimated at 843 ha is already harvested in  the Delta, 199 ha in Matam, 31 ha in Bakel, and 23 ha in Dagana.",
      "PlantingArea",
      Seq(("Area", Seq(("843 ha", "843.0 ha"), ("199 ha", "199.0 ha"), ("31 ha", "31.0 ha"), ("23 ha", "23.0 ha"))))
    ),

  )



  variableTests.zipWithIndex.foreach { case (variableTest, index) => variableTest.test(index) }
}
