package org.clulab.habitus.variables

import org.clulab.habitus.utils.Test
import org.clulab.odin.Mention

import scala.collection.mutable.ArrayBuffer

class TestEntities extends Test {
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
          //          println(targetMentionTexts)

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
        "Quantity" -> Seq("5 t ha-1"))
    ),
    VariableTest(
      "sent2",
      "The potential grain yield that can be obtained ranges from 8 to 9 t ha-1 in the wet season (July sowing) and from 6 to 11 t ha-1 in the dry season (February sowing)",
      Seq("Yield" -> Seq("yield"),
        "Quantity" -> Seq("from 8 to 9 t ha-1", "from 6 to 11 t ha-1"),
        "WetSeason"-> Seq("wet season"),
        "DrySeason"-> Seq("dry season"),
        "Planting"-> Seq("sowing", "sowing"),
        "Date"-> Seq("July", "February")
      )
    ),
    VariableTest(
      "sent3",
      "Double cropping (growing rice in the wet season and dry season on the same field) is possible.",
      Seq(
        "Crop" -> Seq("rice"),
        "WetSeason"-> Seq("wet season"),
        "DrySeason"-> Seq("dry season"),
        "GenericCrop"-> Seq("cropping")
      )
    ),
    VariableTest(
      "sent4",
      "irrigation rules resulted in great variability of irrigation frequency between fields, and sub-optimal timing of nitrogen fertilizer application resulted in yield losses",
      Seq("Yield" -> Seq("yield"),
        "Fertilizer" -> Seq("nitrogen"),
        "GenericFertilizer"-> Seq("fertilizer"),
        "FertilizerUse"-> Seq("fertilizer application")
      )
    ),
    VariableTest(
      "sent5",
      "The potential yields of these three cultivars are similar and are on average about 8 to 9 t ha-1 in the wet season",
      Seq("Yield" -> Seq("yields"),
        "WetSeason" -> Seq("wet season"),
        "Quantity" -> Seq("8 to 9 t ha-1"),
        "GenericCrop"-> Seq("cultivars")
      )
    ),
    VariableTest(
      "sent6",
      "in the 1999WS, with an average grain yield of 7.2 t ha–1. In the 2000WS",
      Seq("Yield" -> Seq("yield"),
        "Quantity" -> Seq("7.2 t ha-1"),
      )
    ),
    VariableTest(
      "sent7",
      "the average grain yield was 8.2 t ha–1",
      Seq("Yield" -> Seq("yield"),
        "Quantity" -> Seq("8.2 t ha-1"),
      )
    ),
    VariableTest(
      "sent8",
      "Average yield reached 7.2 t ha–1 in 1999 and 8.2 t ha–1 in 2000",
      Seq("Yield" -> Seq("yield"),
        "Quantity" -> Seq("7.2 t ha-1", "8.2 t ha-1")
      )
    ),
    VariableTest(
      "sent9",
      "Potential rice grain yields (limited by solar radiation and temperature only) are on average about 9 t ha–1 in the wet growing season from July to November",
      Seq("Yield" -> Seq("yields"),
        "Crop" -> Seq("rice"),
        "Quantity" -> Seq("9 t ha-1"),
        "DateRange" -> Seq("from July to November"),
        "WetSeason" -> Seq("wet growing season")
      )
    ),
    VariableTest(
      "sent10",
      "The Senegal River Valley, located in Sahel zone, is one of the major irrigated rice-producing areas in Senegal.",
      Seq("Location" -> Seq("Senegal", "Sahel", "Senegal River Valley"),
      )
    ),
    VariableTest(
      "sent11",
      "In all scenarios, Sahel 108 variety is sown.",
      Seq("Crop" -> Seq("Sahel 108"),
        "GenericCrop" -> Seq("variety")
      )
    ),
    VariableTest(
      "sent12",
      "These correspond to the dry season (from February/March to June/July).",
      Seq("DrySeason" -> Seq("dry season"),
        "DateRange" -> Seq("March to June", "from February"),
        "Date" -> Seq("July")
      )
    ),
    VariableTest(
      "sent13",
      "Farmers’ yields are on average between 4 and 5 t ha-1, and, therefore, far below potential.",
      Seq("Yield" -> Seq("yields"),
        "Quantity" -> Seq("between 4 and 5 t ha-1")
      )
    ),
    VariableTest(
      "sent14",
      "identified sub-optimal timing of crop management interventions (i.e. date of sowing or trans- planting, herbicide and fertilizer applications, and harvest and post-harvest activities) as major constraints",
      Seq(
        "Planting" -> Seq("sowing", "planting"),
        "GenericFertilizer" -> Seq("fertilizer"),
        "GenericCrop" -> Seq("crop"),
        "FertilizerUse" -> Seq("fertilizer applications")
      )
    ),
    VariableTest(
      "sent15",
      "Farmers generally did not follow recommended sowing time, fertilizer rates, and timings – they generally sowed late, applied late and in excess.",
      Seq(
        "Planting" -> Seq("sowing time", "sowed"),
        "GenericFertilizer" -> Seq("fertilizer"),
        "FertilizerUse" -> Seq("fertilizer")
      )
    ),
    VariableTest(
      "sent16",
      "recommended N rates by Société Nationale d’Aménagement et d’Exploitation des Terres du Delta du fleuve Sénégal et des Vallées du fleuve Sénégal (SAED) range from 133 to 179 kg/ha, applied as di-ammonium phosphate (DAP) at around sowing or transplanting, and three splits of urea (at early tillering, panicle initiation, and booting)",
      Seq(
        "Fertilizer" -> Seq("phosphate", "DAP", "urea", "N", "S", "S"),
        "Planting" -> Seq("sowing", "transplanting"),
        "Quantity" -> Seq("from 133 to 179 kg/ha"),
      )
    ),
    VariableTest(
      "sent17",
      "The recommended rate is higher in the dry season (Haefele and Wopereis, 2004), and higher in the Senegal River delta than in the middle valley, where extreme tem- peratures tend to occur and affect potential yield.",
      Seq(
        "Date" -> Seq("2004"),
        "DrySeason" -> Seq("dry season"),
        "Location" -> Seq("Senegal River"),
        "Yield" -> Seq("yield")
      )
    ),
    VariableTest(
      "sent18",
      "Seeding dates ranged from 22 August to 26 September in 2011WS, from 29 February to 1 April in the 2012DS, and from 5 to 23 March in the 2013DS.",
      Seq(
        "DateRange" -> Seq("from 22 August to 26 September", "from 29 February to 1 April", "from 5 to 23 March"),
        "GenericCrop" -> Seq("Seeding"),
        "Planting" -> Seq("Seeding dates"),
      )
    ),
    VariableTest(
      "sent19",
      "In transplanted rice, seedling age at transplanting was between 26 and 35 days. All farmers grew improved varieties",
      Seq(
        "GenericCrop" -> Seq("varieties", "seedling"),
        "Crop" -> Seq("rice"),
        "Quantity" -> Seq("between 26 and 35 days"),
        "Planting" -> Seq("transplanted")
      )
    ),
    VariableTest(
      "sent20",
      "Popular varieties in the wet season were Sahel 202 (65% of farmers) and Sahel 201 (30%), while 60% and 92% grew Sahel 108 in 2012DS and 2013DS, respectively.",
      Seq(
        "Crop" -> Seq("Sahel", "Sahel 202", "Sahel 201"),
        "GenericCrop" -> Seq("varieties"),
        // FIXME, not exactly an ammount!
        "Quantity" -> Seq("108 in"),
        "WetSeason" -> Seq("wet season")
      )
    ),
    VariableTest(
      "sent21",
      "timing of basal fertilizer application was on average 26, 33, and 26 days after sowing (DAS) in 2011WS, 2012DS, and 2013DS",
      Seq(
        "FertilizerUse" -> Seq("fertilizer application"),
        "Quantity" -> Seq("26 days"),
        "Planting" -> Seq("sowing"),
        "GenericFertilizer" -> Seq("fertilizer")
      )
    ),
    VariableTest(
      "sent22",
      "timing of first top-dressing of N on average for each season was 59–61 DAS",
      Seq(
        "Fertilizer" -> Seq("N")
      )
    ),
    VariableTest(
      "sent23",
      "WS sowing in July and about 9–10 t ha−1 for dry season (DS) sowing in February in the Senegal River delta.",
      Seq(
        "Quantity" -> Seq("–10 t"),
        "Planting" -> Seq("sowing", "sowing"),
        "Location" -> Seq("Senegal River"),
        "DrySeason" -> Seq("dry season"),
        "Date" -> Seq("February", "July"),
      )
    ),
    VariableTest(
      "sent24",
      "Major biophysical constraints included phosphorus (P) deficient soils, weed competition, relatively low nitrogen (N) application rates and sub-optimal timing of N application.",
      Seq(
        "Fertilizer" -> Seq("N", "N", "phosphorus", "P", "nitrogen")
      )
    ),
    VariableTest(
      "sent25",
      "Most farmers in the Senegal River valley apply N fertilizer only twice, i.e. roughly at the start of tillering and panicle initiation (PI).",
      Seq(
        "Location" -> Seq("Senegal River"),
        "GenericFertilizer" -> Seq("fertilizer"),
        "FertilizerUse" -> Seq("fertilizer"),
        "Fertilizer" -> Seq("N")
      )
    ),
  VariableTest(
    "sent26",
    "Farmers applied N fertilizer twice, i.e. at the start of tillering and at PI,",
    Seq(
      "Location" -> Seq("PI"),
      "GenericFertilizer" -> Seq("fertilizer"),
      "FertilizerUse" -> Seq("fertilizer"),
      "Fertilizer" -> Seq("N")
    )
  ),
    VariableTest(
      "sent27",
      "Farmers used two rice cultivars (IR1529 and Jaya) during the 1997 WS, and exclusively IR13240-108-2-2-3 (released as Sahel 108 in Senegal in 1994) during the 1998 DS. All rice was direct-seeded.",
      Seq(
        "GenericCrop" -> Seq("direct-seeded", "cultivars"),
        "Crop" -> Seq("rice", "Sahel", "Jaya", "rice"),
        "Quantity" -> Seq("108 in", "1998 DS"),
        "Location" -> Seq("Senegal"),
        "Date" -> Seq("1994", "1997")

      )
    ),
    VariableTest(
      "sent27",
      "Average WS T0 yield was high, i.e. 7.3 ha−1 (ranging from 5.0 to 9.4 t ha−1), considering the region’s potential yield of about 9 t ha−1.",
      Seq(
        "Yield" -> Seq("yield", "yield"),
        "Quantity" -> Seq("9 t", "from 5.0 to 9.4 t", "7.3 ha")
      )
    ),
    VariableTest(
      "sent28",
      "Average DS T0 yield was relatively low, i.e. 4.4 t ha− 1 (ranging from 2.5 to 6.0 t ha− 1)",
      Seq(
        "Yield" -> Seq("yield"),
        "Quantity" -> Seq("4.4 t", "from 2.5 to 6.0 t")
      )
    ),
    VariableTest(
      "sent29",
      "The favourable climate conditions and the adoption of varieties with shorter cropping cycles allow for two rice harvests per year, namel in the dry and rainy seasons (Van Oort et al., 2016).",
      Seq(
        "GenericCrop" -> Seq("varieties", "cropping"),
        //FIXME; seasons separated by 'and' extracted as one entity.
        "DrySeason" -> Seq("dry and rainy seasons"),
        "Crop" -> Seq("rice"),
        "Date" -> Seq("2016")
      )
    ),
    VariableTest(
      "sent30",
      "In the SRV, production areas are typically larger in the dry season, which brings fewer problems with pests and birds (Tanaka et al., 2015; USDA-GAIN, 2018).",
      Seq(
        "DrySeason" -> Seq("dry season"),
        "Date" -> Seq("2015", "2018")
      )
    ),
    VariableTest(
      "sent31",
      "Average yields in SRV theoretically range between 5.0 and 6.0 t ha− 1 in the rainy season and between 6.5 and 7.5 t ha− 1 in the dry season (SAED, 2019; USDA-GAIN, 2021)",
      Seq(
        "Yield" -> Seq("yields"),
        "DrySeason" -> Seq("dry season"),
        "Date" -> Seq("2021", "2019"),
        "Quantity" -> Seq("− 1 in", "between 6.5 and 7.5 t", "− 1 in", "between 5.0 and 6.0 t"),
        // FIXME; this is not  a location, i think.
        "Location" -> Seq("USDA-GAIN")
      )
    ),
    VariableTest(
      "sent32",
      "seeds of the rice variety Sahel 108 are sown, a short- cycle variety (around 125 days)",
      Seq(
        "Quantity" -> Seq("125 days"),
        "Planting" -> Seq("sown"),
        "GenericCrop" -> Seq("variety", "variety", "seeds"),
        "Crop" -> Seq("rice", "Sahel 108")
      )
    ),
    VariableTest(
      "sent33",
      "Broadcast seeding is carried out by hand on irrigated plots with a 2–5 cm depth sheet of water",
      Seq(
        //FIXME; range instead of -5 cm
        "Quantity" -> Seq("–5 cm"),
        "Planting" -> Seq("seeding"),
        "GenericCrop" -> Seq("seeding")
      )
    ),
    VariableTest(
      "sent34",
      "only two top dressings are applied in the CONV scenario ( the first one with urea and dia- mmonium phosphate (DAP) at the beginning of tillering; and the second only with urea at panicle initiation.",
      Seq(
        //FIXME; not a location
        "Location" -> Seq("CONV"),
        "Fertilizer" -> Seq("urea", "DAP", "phosphate", "urea"),
      )
    ),
    VariableTest(
      "sent35",
      "In INT, a basic dressing is firstly applied with urea and DAP, followed by two top-dressing applications with urea.",
      Seq(
        "Fertilizer" -> Seq("urea", "DAP", "urea"),
      )
    ),

    VariableTest(
      "sent36",
      "average yields for the two seasons assessed of 4832 kg ha− 1 and 7425 kg ha− 1 for the areas under CONV and INT management, respectively.",
      Seq(
        "Yield" -> Seq("yields"),
        "Quantity" -> Seq("4832 kg", "7425 kg")
      )
    ),
    VariableTest(
      "sent37",
      "Agricultural inputs, machinery operations and yields considered in the four scenarios evaluated: conventional (CONV), intensive (INT), and the two refer- ence scenarios SAED_2td and SAED_3td. DAP: Diammonium phosphate.",
      Seq(
        "FertilizerUse" -> Seq("inputs"),
        "Yield" -> Seq("yields"),
        "Fertilizer" -> Seq("DAP", "Diammonium phosphate")
      )
    ),
    VariableTest(
      "sent38",
      "Fertilisers considered and associated nutrients in the four scenarios evaluated: conventional (CONV), intensive (INT), and the two reference scenarios SAED_2td and SAED_3td. DAP: diammonium phosphate.",
      Seq(
        "Fertilizer" -> Seq("DAP", "diammonium phosphate")
      )
    ),
    VariableTest(
      "sent39",
      "In plots receiving fertilizer, DAP was applied basally (19.3 and 21.5 kg N and P ha−1 ), and three urea splits were broadcasted into 1–5 cm of water (101.3 kg N ha−1 ; 40% at early-tillering, 40% at pan- icle initiation, and 20% at heading)",
      Seq(
        "Fertilizer" -> Seq("DAP", "N", "P", "urea", "N"),
        "Quantity" -> Seq("101.3 kg", "–5 cm", "21.5 kg"),
        "GenericFertilizer" -> Seq("fertilizer"),
        "FertilizerUse" -> Seq("fertilizer"),
        "Fertilizer" -> Seq("N", "urea", "P", "N", "DAP")
      )
    ),
    VariableTest(
      "sent39",
      "The short-duration O. sativa cultivar Sahel-108 (IR-13240-108- 2-2-3) was grown during all seasons.",
      Seq(
        // FIXME; Sahel-108 not extracting here;
        "GenericCrop" -> Seq("cultivar"),
      )
    ),
    VariableTest(
      "sent39",
      "Transplanting for both management systems took place on 7 (2007 and 2008 wet season) and 25 (2009 wet season) August and on 19 March (2008 and 2009 dry season).",
      Seq(
        "Date" -> Seq("2007", "2008", "2009", "August", "19 March", "2008", "2009"),
        "DrySeason" -> Seq("dry season"),
        "Planting" -> Seq("Transplanting"),
        "WetSeason" -> Seq("wet season", "wet season")
      )
    ),
    VariableTest(
      "sent40",
      "Year, site, crop management, soil properties, and rainfall distribution in the experiments in Senegal used for DSSAT calibration and validation.",
      Seq(
        "Location" -> Seq("Senegal"),
        "GenericCrop" -> Seq("crop")
      )
    ),
    VariableTest(
      "sent41",
      "das days after sowing, Fert fertilizer treatment, with F1: recommended dose (80 kg N ha−1), i.e., 200 kg ha−1 NPK (15.15.15) at sowing + 100 kg ha−1 urea at 20 das + 50 kg ha−1 urea at 50 das. F2: F1/4 (20 kg N ha−1);",
      Seq(
        "Fertilizer" -> Seq("N", "NPK", "urea", "urea" ),
        "FertilizerUse" -> Seq("fertilizer"),
        "GenericFertilizer" -> Seq("fertilizer"),
        "Planting" -> Seq("sowing", "sowing"),
        "Quantity" -> Seq("80 kg", "200 kg", "+ 100 kg", "+ 50 kg")
      )
    ),



  )


  variableTests.zipWithIndex.foreach { case (variableTest, index) => variableTest.test(index) }
}


