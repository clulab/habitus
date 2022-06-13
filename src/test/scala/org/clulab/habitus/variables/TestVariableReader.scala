package org.clulab.habitus.variables

import org.clulab.habitus.utils.Test
import org.clulab.odin.{Mention, TextBoundMention}

class TestVariableReader extends Test {
  val vp: VariableProcessor = VariableProcessor()

  // (variableText, Seq[(valueText, valueNorm)])
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
      parsingResults.targetMentions
    }

    def test(index: Int): Unit = {
      it should s"process $index-$name correctly" in {
        if (index == -1)

          println("Put a breakpoint here to observe a particular test.")

        val allmentions = getMentions(text)
        val mentions = allmentions.filter(_.label matches label).sortBy(_.tokenInterval)
        if (variables.isEmpty) {
          // get only relations and events
          val nonTextBoundMentions = mentions.filterNot(m => m.isInstanceOf[TextBoundMention])
          // there should be none
          nonTextBoundMentions.length should be (0)
        } else {

          mentions should have size variables.length

          variables.zip(mentions).zipWithIndex.foreach { case ((variable, mention), variableIndex) =>
            (variableIndex, mention.arguments("variable").head.text) should be((variableIndex, variable._1))

            val values = variable._2
            val arguments = mention.arguments("value")

            arguments should have size values.length
            values.zip(arguments).zipWithIndex.foreach { case ((value, argument), valueIndex) =>
              (variableIndex, valueIndex, argument.text) should be ((variableIndex, valueIndex, value._1))
              (variableIndex, valueIndex, argument.norms.get.head) should be ((variableIndex, valueIndex, value._2))
            }
          }
        }

      }
    }
  }

  behavior of "VariableReader"

  val variableTests: Array[VariableTest] = Array(
    // The CLU parser breaks on this one, but the SRL works fine!
    VariableTest(
      "sent1", "Farmers’ sowing dates ranged from 14 to 31 July for the WS and from 3 to 11 March for the DS.",
      "PlantingDate",
      Seq(
        ("sowing dates", Seq(("from 14 to 31 July", "XXXX-07-14 -- XXXX-07-31"))),
        ("sowing dates", Seq(("from 3 to 11 March", "XXXX-03-03 -- XXXX-03-11")))
      )
    ),
    VariableTest(
      "sent2", "Sowing between October 4 and October 14 was optimal.",
      "PlantingDate",
      Seq(("Sowing", Seq(("between October 4 and October 14", "XXXX-10-04 -- XXXX-10-14"))))
    ),
    VariableTest(
      "sent3", "Sowing date was October 7, 2019 .",
      "PlantingDate",
      Seq(("Sowing date", Seq(("October 7, 2019", "2019-10-07"))))
    ),
    VariableTest(
      "sent4", "Sowing between October 4 and October 14 of 2020 was optimal.",
      "PlantingDate",
      Seq(("Sowing", Seq(("between October 4 and October 14 of 2020", "2020-10-04 -- 2020-10-14"))))
    ),
    VariableTest(
      "sent5", "Planting date was October 1.",
      "PlantingDate",
      Seq(("Planting date", Seq(("October 1", "XXXX-10-01"))))
    ),
    VariableTest(
      "sent6", "Planting date was October 1 of 2021.",
      "PlantingDate",
      Seq(("Planting date", Seq(("October 1 of 2021", "2021-10-01"))))
    ),
    VariableTest(
      "sent7", "Planting occurred on October 2.",
      "PlantingDate",
      Seq(("Planting", Seq(("October 2", "XXXX-10-02"))))
    ),
    VariableTest(
      "sent8", "Planting date was October 4, 2019",
      "PlantingDate",
      Seq(("Planting date", Seq(("October 4, 2019", "2019-10-04"))))
    ),
    VariableTest(
      "sent9", "Planting occurred on October 1, 2019.",
      "PlantingDate",
      Seq(("Planting", Seq(("October 1, 2019", "2019-10-01"))))
    ),
    VariableTest(
      "sent10", "Sowing occurred on October 1, 2019",
      "PlantingDate",
      Seq(("Sowing", Seq(("October 1, 2019", "2019-10-01"))))
    ),
    VariableTest(
      "sent10_2", "Seeding occurred on October 1, 2019",
      "PlantingDate",
      Seq(("Seeding", Seq(("October 1, 2019", "2019-10-01"))))
    ),
    VariableTest(
      "sent11", "Sowing on October 9, 2019.",
      "PlantingDate",
      Seq(("Sowing", Seq(("October 9, 2019", "2019-10-09"))))
    ),
    VariableTest(
      "sent11_2", "Planting on April 7, 2019",
      "PlantingDate",
      Seq(("Planting", Seq(("April 7, 2019", "2019-04-07"))))
    ),
    VariableTest(
      "sent12", "Sowing between October 4 and October 14 of 2020 was optimal.",
      "PlantingDate",
      Seq(("Sowing", Seq(("between October 4 and October 14 of 2020", "2020-10-04 -- 2020-10-14"))))
    ),
    VariableTest(
      "sent12_1", "Sowing dates between October 4 and October 14, 2020 was optimal.",
      "PlantingDate",
      Seq(("Sowing dates", Seq(("between October 4 and October 14, 2020", "2020-10-04 -- 2020-10-14"))))
    ),
    VariableTest(
      "sent13", "Sowing in May, 2019.",
      "PlantingDate",
      Seq(("Sowing", Seq(("May, 2019", "2019-05-XX"))))
    ),
    VariableTest(
      "sent14", "Sowing in April, 2011 could increase outputs.",
      "PlantingDate",
      Seq(("Sowing", Seq(("April, 2011", "2011-04-XX"))))
    ),
    VariableTest(
      "sent14_1", "Planting in April, 2011 is recommended for the year.",
      "PlantingDate",
      Seq(("Planting", Seq(("April, 2011", "2011-04-XX"))))
    ),
    VariableTest(
      "sent15", "Sowing on September 8 yields best outcomes.",
      "PlantingDate",
      Seq(("Sowing", Seq(("September 8", "XXXX-09-08"))))
    ),
    //TODO: ms, but without [, year] it does not complain.
    VariableTest(
      "sent15_2", "Sowing on April 8 yields best outcomes.",
      "PlantingDate",
      Seq(("Sowing", Seq(("April 8", "XXXX-04-08"))))
    ),
    VariableTest(
      "sent15_3", "Sowing on April 7, 2001 yields best outcomes.",
      "PlantingDate",
      Seq(("Sowing", Seq(("April 7, 2001", "2001-04-07"))))
    ),
    VariableTest(
      "sent15_4", "The first sowing dates started on July 1st in 2010 and on July 8th in 2011",
      "PlantingDate",
      Seq(
        ("sowing dates", Seq(("July 1st", "XXXX-07-01"))),
        ("sowing dates", Seq(("2010", "2010-XX-XX")))
//        ("sowing dates", Seq(("July 8th", "XXXX-07-08"))),
//        ("sowing dates", Seq(("2011", "2011-XX-XX")))
      )
    ),
    VariableTest(
      "sent15_5", "sowing ( July 15 )",
      "PlantingDate",
      Seq(("sowing", Seq(("July 15", "XXXX-07-15"))))
    ),
    VariableTest(
      "sent15_6", "Early sowing (before July 15)",
      "PlantingDate",
      Seq(("Early sowing", Seq(("before July 15", "XXXX-XX-XX -- XXXX-07-15"))))
    ),
    VariableTest(
      "sent15_7", "Late sowings (beyond August 15) total 82% achievements either 19,762 ha.",
      "PlantingDate",
      Seq(("Late sowings", Seq(("beyond August 15", "XXXX-08-15 -- XXXX-XX-XX"))))
    ),
    VariableTest(
      "sent16_7", "sowing was done on 25th October",
      "PlantingDate",
      Seq(("sowing", Seq(("25th October", "XXXX-10-25"))))
    ),
    VariableTest(
      "sent16_8", "sowing date began on July 8th",
      "PlantingDate",
      Seq(("sowing date", Seq(("July 8th", "XXXX-07-08"))))
    ),
    VariableTest(
      "sent16_9", "planting around July 15th led to the highest aerial biomass and grain yield for both years",
      "PlantingDate",
      Seq(("planting", Seq(("around July 15th", "XXXX-07-15 [APPROX]"))))
    ),
    VariableTest(
      "sent16_10", "Sowing after July 15 th reduced aerial biomass and grain yield",
      "PlantingDate",
      Seq(("Sowing", Seq(("after July 15", "XXXX-07-15 -- XXXX-XX-XX"))))
    ),
    // Tests for CULTIVARS
    VariableTest(
      "sent16", "The most important planted cash crop is peanut in the SRV.",
      "CropAssignment",
      Seq(("crop", Seq(("peanut", ""))))
    ),
    VariableTest(
      "sent16_0", "Most farmers in the SRV plant Sahel 108",
      "CropAssignment",
      Seq(("plant", Seq(("Sahel 108", ""))))
    ),
    VariableTest(
      "sent16_1", "Some farmers use variety like Sahel 108",
      "CropAssignment",
      Seq(("variety", Seq(("Sahel 108", ""))))
    ),
    VariableTest(
      "sent16_2", "The productivity of a range of agricultural crops beyond rice",
      "CropAssignment",
      Seq(("crops", Seq(("rice", ""))))
    ),
    VariableTest(
      "sent16_3", "In the SRV where irrigated rice is the most common grown crop",
      "CropAssignment",
      Seq(("crop", Seq(("rice", ""))))
    ),
    VariableTest(
      "sent16_2_1", "Farmers use cultivar such as sugarcane which are planted on 80 and 20 percent of total area",
      "CropAssignment",
      Seq(("cultivar", Seq(("sugarcane", ""))))
    ),
    VariableTest(
      "sent16_2_2", "AfricaRice, a CGIAR research center, developed the seed Sahel 108",
      "CropAssignment",
      Seq(("seed", Seq(("Sahel 108", ""))))
    ),
    VariableTest(
      "sent16_2_3", "Farmers preferred to use short duration rice varieties like Sahel 108",
      "CropAssignment",
      Seq(("varieties", Seq(("Sahel 108", ""))))
    ),
    VariableTest(
      "sent16_2_4", "Other crops cultivated include millet",
      "CropAssignment",
      Seq(("crops", Seq(("millet", ""))))
    ),
    VariableTest(
      "sent16_3_1", "Other crops cultivated include millet, sorghum, maize, cowpea and vegetables",
      "CropAssignment",
      Seq(
        ("crops", Seq(("millet", ""))),
        ("crops", Seq(("sorghum", ""))),
        ("crops", Seq(("maize", ""))),
        ("crops", Seq(("cowpea", ""))),
        ("crops", Seq(("vegetables", "")))
      )
    ),
    VariableTest(
      "sent16_4", "In the 1998WS, farmers sowed Jaya between 20 June and 1 July",
      "PlantingEvent",
      // The second one of these wasn't initially tested.
      Seq(("sowed",Seq(("Jaya", ""), ("between 20 June and 1 July", "XXXX-06-20 -- XXXX-07-01"))))
    ),
    VariableTest(
      "birdattack_1", "Bird attacks occurred between July 1st and August 31st",
      "BirdAttackEvent",
      Seq(("Bird attacks", Seq(("between July 1st and August 31st", "XXXX-07-01 -- XXXX-08-31"))))
    ),
    VariableTest(
      "sent16_4_1", "They chose furthermore to grow only one cultivar groundnut",
      "CropAssignment",
      Seq(("cultivar", Seq(("groundnut", ""))))
    ),
    VariableTest(
      "sent16_4_2", "In the SRV, farmers plant Sahel 108, Sahel 150, Sahel 154, Sahel 134, Nerica",
      "PlantingEvent",
      Seq(
        ("plant", Seq(("Sahel 108", ""))),
        ("plant", Seq(("Sahel 150", ""))),
        ("plant", Seq(("Sahel 154", ""))),
        ("plant", Seq(("Sahel 134", ""))),
        ("plant", Seq(("Nerica", "")))
      )
    ),
    VariableTest(
      "sent17_1", "Peanut, sugarcane and cotton are important cash crops.",
      "CropAssignment",
      Seq(
        ("crops", Seq(("Peanut", ""))),
        ("crops", Seq(("sugarcane", ""))),
        ("crops", Seq(("cotton", "")))
      )
    ),
    VariableTest(
      "sent17_2", "Millet, rice, corn and sorghum are the primary food crops grown in Senegal.",
      "CropAssignment",
      Seq(
        ("crops", Seq(("Millet", ""))),
        ("crops", Seq(("rice", ""))),
        ("crops", Seq(("corn", ""))),
        ("crops", Seq(("sorghum", "")))
      )
    ),
    VariableTest(
      "sent17_3", "Tomato or onion were the two most labour-consuming crops",
      "CropAssignment",
      Seq(
        ("crops", Seq(("Tomato", ""))),
        ("crops", Seq(("onion", "")))
      )
    ),
    VariableTest(
      "sent17_4", "Onion and tomato were the most profitable crops.",
      "CropAssignment",
      Seq(
        ("crops", Seq(("Onion", ""))),
        ("crops", Seq(("tomato", "")))
      )
    ),
    // Tests for Fertilizer var-val reading
    VariableTest(
      "sent20", "One of the most important farming input is mineral fertilizer",
      "FertilizerAssignment",
      Seq(("fertilizer", Seq(("mineral", ""))))
    ),
    //
    VariableTest(
      "sent20_1", "Fertilizer nitrogen (N) has been applied at two or more levels",
      "FertilizerAssignment",
      Seq(("Fertilizer", Seq(("nitrogen", ""))))

    ),
    VariableTest(
      "sent20_2", "In fact, use of fertilizer P has declined steadily since 1995",
      "FertilizerAssignment",
      Seq(("fertilizer", Seq(("P", ""))))
    ),
    VariableTest(
      "sent20_3", "The amount of fertilizer N required was averaged at 52 kg ha-1",
      "FertilizerAssignment",
      Seq(("fertilizer", Seq(("N", ""))))
    ),
    VariableTest(
      "sent20_4", "The most widely used solid inorganic fertilizers are urea, diammonium phosphate and potassium chloride",
      "FertilizerAssignment",
      Seq(
        ("fertilizers", Seq(("urea", ""))),
        ("fertilizers", Seq(("diammonium phosphate", ""))),
        ("fertilizers", Seq(("potassium chloride", "")))
      )
    ),
    VariableTest(
      "sent20_5", "The most widely used solid inorganic fertilizers are diammonium phosphate, urea and potassium chloride",
      "FertilizerAssignment",
      Seq(
        ("fertilizers", Seq(("diammonium phosphate", ""))),
        ("fertilizers", Seq(("urea", ""))),
        ("fertilizers", Seq(("potassium chloride", "")))
      )
    ),
    VariableTest(
      "sent20_5_1", "The main nutrient elements present in the fertilizers are nitrogen, phosphorus, and potassium",
      "FertilizerAssignment",
      Seq(
        ("fertilizers", Seq(("nitrogen", ""))),
        ("fertilizers", Seq(("phosphorus", ""))),
        ("fertilizers", Seq(("potassium", "")))
      )
    ),
    VariableTest(
      "sent20_5_2", "The nitrogenous chemical fertilizers are urea, calcium, ammonium nitrate, ammonium sulfate, basic calcium nitrate, calcium cyanamide",
      "FertilizerAssignment",
      Seq(
        ("fertilizers", Seq(("chemical", ""))), // note: similar to `mineral fertilizer` in sent20
        ("fertilizers", Seq(("urea", ""))),
        ("fertilizers", Seq(("calcium", ""))),
        ("fertilizers", Seq(("ammonium nitrate", ""))),
        ("fertilizers", Seq(("ammonium sulfate", ""))),
        ("fertilizers", Seq(("basic calcium nitrate", ""))),
        ("fertilizers", Seq(("calcium cyanamide", "")))
      )
    ),
    VariableTest(
      "sent20_6", "Total fertilizer usage was ammonium poly-phosphate on average 152 kg ha-1 in the 1999 and 2000WS",
      "FertilizerAssignment",
      Seq(("fertilizer", Seq(("ammonium poly-phosphate", ""))))
    ),
    VariableTest(
      "sent20_7", "However, the most unvalable fertilizer was diammonium-phosphate",
      "FertilizerAssignment",
      Seq(("fertilizer", Seq(("diammonium-phosphate", ""))))
    ),
    VariableTest(
      "sent20_8", "Phosphorus, potassium and NPK are important inorganic fertilizers.",
      "FertilizerAssignment",
      Seq(
        ("fertilizers", Seq(("Phosphorus", ""))),
        ("fertilizers", Seq(("potassium", ""))),
        ("fertilizers", Seq(("NPK", "")))
      )
    ),
    VariableTest(
      "sent20_9", "Some organic fertilizers include nitrogen, phosphorus, and potassium as the three most important elements for plant nutrition",
      "FertilizerAssignment",
      Seq(
        ("fertilizers", Seq(("nitrogen", ""))),
        ("fertilizers", Seq(("phosphorus", ""))),
        ("fertilizers", Seq(("potassium", "")))
      )
    ),
    // Tests for sentences found in SAED bulletins Dec 2021
    VariableTest(
      "sent21_1", "Sowing (15 July - 15 August) concentrates 20% of the sown areas, or 4,239 ha;",
      "PlantingDate",
      Seq(("Sowing", Seq(("15 July - 15 August", "XXXX-07-15 -- XXXX-08-15"))))
    ),
    VariableTest(
      "sent21_2", " Early sowing (before July 15) covers 1% of the total areas developed, ie 242 ha.",
      "PlantingDate",
      Seq(("Early sowing", Seq(("before July 15", "XXXX-XX-XX -- XXXX-07-15"))))
    ),
    VariableTest(
      "sent21_3", "Late sowing (beyond August 15) accounts for 79% of the projects, ie 17,043 ha.",
      "PlantingDate",
      Seq(("Late sowing", Seq(("beyond August 15", "XXXX-08-15 -- XXXX-XX-XX"))))
    ),
    VariableTest(
      "sent21_4", "early sowing (month of January) occupies a small proportion of cultivated areas.",
      "PlantingDate",
      Seq(("early sowing", Seq(("month of January", "XXXX-01-XX"))))
    ),
    VariableTest(
      "sent21_5", "Thus, the rate of sowing intensified beyond February 25, 2020 with a marked increase in the areas sown, particularly in Dagana.",
      "PlantingDate",
      Seq(("sowing", Seq(("beyond February 25, 2020", "2020-02-25 -- XXXX-XX-XX"))))
    ),
    VariableTest(
      "sent21_6", "The late start of the campaign, due among other constraints to the delay in holding the 1er credit committee, has had an impact on the crop calendar, in particular with so-called late sowing (beyond March 17) which takes up a large part of the development",
      "PlantingDate",
      Seq(("late sowing", Seq(("beyond March 17", "XXXX-03-17 -- XXXX-XX-XX"))))
    ),
    VariableTest(
      "sent21_7", "Overall, it is noted that 65% of the areas are developed beyond September 15, 2020 (late sowing), the areas sown during the recommended period (between July 15 and August 15) cover 34% of the plantings and early sowing (before mid-July) represents 1% of the total development.",
      "PlantingDate",
      Seq(
        ("late sowing", Seq(("beyond September 15, 2020", "2020-09-15 -- XXXX-XX-XX"))),
        ("sown", Seq(("between July 15 and August 15", "XXXX-07-15 -- XXXX-08-15"))),
        ("early sowing", Seq(("before mid July", "XXXX-XX-XX -- XXXX-07-15")))
      )
    ),
    VariableTest(
      "sent21_8", "2% of the areas currently cultivated are sown before February 15, 2020, i.e. 783 ha ;",
      "PlantingDate",
      Seq(("sown", Seq(("before February 15, 2020", "XXXX-XX-XX -- 2020-02-15"))))
    ),
    VariableTest(
      "sent21_9", "In fact, early sowing (month of January) occupies a small proportion of cultivated areas.",
      "PlantingDate",
      Seq(("early sowing", Seq(("month of January", "XXXX-01-XX"))))
    ),
    VariableTest(
      "sent21_10", "To date , the distribution according to the cropping calendar , of the sowing carried out at the level of the Dagana delegation is as follows : - - - 9 % of the areas currently cultivated are sown before February 15 , 2020 , i.e. 3,146.92 ha ; Between the date of February 15 to March 15 , 2020 , are sown 66 % areas developed , i.e. 21,900.73 ha ; Areas sown beyond March 15 , 2020 cover 25 % of the entire development , i.e. 8,278.18 ha",
      "PlantingDate",
      Seq(
        ("sown", Seq(("before February 15 , 2020", "XXXX-XX-XX -- 2020-02-15"))),
        ("sown", Seq(("February 15 to March 15 , 2020", "2020-02-15 -- 2020-03-15"))),
        ("sown", Seq(("beyond March 15 , 2020", "2020-03-15 -- XXXX-XX-XX")))
      )
    ),
    VariableTest(
      "sent21_11", "2 Others : cassava , melon , peanuts , eggplant , watermelon , pepper , carrot , cabbage , jaxatu , beans , bissap , banana plantation , fruit trees , white onion , cucumber , squash …",
      "None",
      Seq.empty
    ),
    VariableTest(
      "sent21_12", "Farmers used four indica cultivars: Aiwu (short duration, slender grain), I Kong Pao (short duration, bold grain) and IR1529 and Jaya (medium duration, slender grain).",
      "PropertyAssignment",
      Seq( ("Aiwu", Seq(("short duration", ""))),
        ("Aiwu", Seq(("slender grain", ""))),
        ("I Kong Pao", Seq(("short duration", ""))),
        ("I Kong Pao", Seq(("bold grain", ""))),
        ("IR1529", Seq(("medium duration", ""))),
        ("IR1529", Seq(("slender grain", ""))),
          ("Jaya", Seq(("medium duration", ""))),
        ("Jaya", Seq(("slender grain", "")))
      )
    ),
    //FIXME; Wrong assignment issues.
    VariableTest(
      "fix1", "Rice cultivation and management are autosufficient in terms of seed supply , irrigation and rice grain milling .",
      "CropAssignment",
      Seq.empty
    ),
    VariableTest(
      "fix2", "Rice yields derived by NMR were compared with yields obtained with farmers ' fertilizer practices ( FFP ) in 20 , 58 , and 24 on-farm trials in the 2011 wet season , 2012 dry season , and 2013 dry season , respectively ( referred to as 2011WS , 2012DS , and 2103DS ) .",
      "DrySeasonAssignment",
      Seq(
        ("DrySeason", Seq(("2012DS", ""))),
        ("DrySeason", Seq(("2013DS", "")))
      )
    ),

  )

  variableTests.zipWithIndex.foreach { case (variableTest, index) => variableTest.test(index) }
}
