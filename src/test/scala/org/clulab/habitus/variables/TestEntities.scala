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
                           shouldable: Shouldable,
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
      shouldable should s"filter by correct label(s) and value size of $name correctly" in {
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


          val targetMentionTexts = targetMentions.map(_.text) // text of similar kinds but for norms(
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
      passingTest,
      "sent1",
      "with an average yield over years yields and seasons of 5 t ha-1",
      Seq(
        "Yield" -> Seq("yield", "yields"),
        "Quantity" -> Seq("5 t ha-1")
      )
    ),
    VariableTest(
      passingTest,
      "sent2",
      "The potential grain yield that can be obtained ranges from 8 to 9 t ha-1 in the wet season (July sowing) and from 6 to 11 t ha-1 in the dry season (February sowing)",
      Seq(
        "Yield" -> Seq("yield"),
        "Quantity" -> Seq("from 8 to 9 t ha-1", "from 6 to 11 t ha-1"),
        "WetSeason"-> Seq("wet season"),
        "DrySeason"-> Seq("dry season"),
        "Planting"-> Seq("sowing", "sowing"),
        "Date"-> Seq("July", "February")
      )
    ),
    VariableTest(
      passingTest,
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
      passingTest,
      "sent4",
      "irrigation rules resulted in great variability of irrigation frequency between fields, and sub-optimal timing of nitrogen fertilizer application resulted in yield losses",
      Seq(
        "Yield" -> Seq("yield"),
        "Fertilizer" -> Seq("nitrogen"),
        "GenericFertilizer"-> Seq("fertilizer"),
        "FertilizerUse"-> Seq("fertilizer application")
      )
    ),
    VariableTest(
      passingTest,
      "sent5",
      "The potential yields of these three cultivars are similar and are on average about 8 to 9 t ha-1 in the wet season",
      Seq(
        "Yield" -> Seq("yields"),
        "WetSeason" -> Seq("wet season"),
        "Quantity" -> Seq("8 to 9 t ha-1"),
        "GenericCrop"-> Seq("cultivars")
      )
    ),
    VariableTest(
      passingTest,
      "sent6",
      "in the 1999WS, with an average grain yield of 7.2 t ha–1. In the 2000WS",
      Seq(
        "Yield" -> Seq("yield"),
        "Quantity" -> Seq("7.2 t ha-1"),
      )
    ),
    VariableTest(
      passingTest,
      "sent7",
      "the average grain yield was 8.2 t ha–1",
      Seq(
        "Yield" -> Seq("yield"),
        "Quantity" -> Seq("8.2 t ha-1"),
      )
    ),
    VariableTest(
      passingTest,
      "sent8",
      "Average yield reached 7.2 t ha–1 in 1999 and 8.2 t ha–1 in 2000",
      Seq(
        "Yield" -> Seq("yield"),
        "Quantity" -> Seq("7.2 t ha-1", "8.2 t ha-1")
      )
    ),
    VariableTest(
      passingTest,
      "sent9",
      "Potential rice grain yields (limited by solar radiation and temperature only) are on average about 9 t ha–1 in the wet growing season from July to November",
      Seq(
        "Yield" -> Seq("yields"),
        "Crop" -> Seq("rice"),
        "Quantity" -> Seq("9 t ha-1"),
        "DateRange" -> Seq("from July to November"),
        "WetSeason" -> Seq("wet growing season")
      )
    ),
    VariableTest(
      passingTest,
      "sent10",
      "The Senegal River Valley, located in Sahel zone, is one of the major irrigated rice-producing areas in Senegal.",
      Seq(
        "Location" -> Seq("Senegal", "Sahel", "Senegal River Valley"),
      )
    ),
    VariableTest(
      passingTest,
      "sent11",
      "In all scenarios, Sahel 108 variety is sown.",
      Seq(
        "Crop" -> Seq("Sahel 108"),
        "GenericCrop" -> Seq("variety")
      )
    ),
    VariableTest(
      passingTest,
      "sent12",
      "These correspond to the dry season (from February/March to June/July).",

      Seq(
        "DrySeason" -> Seq("dry season"),
        "DateRange" -> Seq("from February/March to June/July")
      )
    ),
    VariableTest(
      passingTest,
      "sent13",
      "Farmers’ yields are on average between 4 and 5 t ha-1, and, therefore, far below potential.",
      Seq(
        "Yield" -> Seq("yields"),
        "Quantity" -> Seq("between 4 and 5 t ha-1")
      )
    ),
    VariableTest(
      passingTest,
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
      passingTest,
      "sent15",
      "Farmers generally did not follow recommended sowing time, fertilizer rates, and timings – they generally sowed late, applied late and in excess.",
      Seq(
        "Planting" -> Seq("sowing time", "sowed"),
        "GenericFertilizer" -> Seq("fertilizer")
      )
    ),
    VariableTest(
      failingTest,
      "sent16",
      "recommended N rates by Société Nationale d’Aménagement et d’Exploitation des Terres du Delta du fleuve Sénégal et des Vallées du fleuve Sénégal (SAED) range from 133 to 179 kg/ha, applied as di-ammonium phosphate (DAP) at around sowing or transplanting, and three splits of urea (at early tillering, panicle initiation, and booting)",
      Seq(
        "Fertilizer" -> Seq("phosphate", "DAP", "urea", "N"),
        "Planting" -> Seq("sowing", "transplanting"),
        "Quantity" -> Seq("from 133 to 179 kg/ha"),
      )
    ),
    VariableTest(
      passingTest,
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
      passingTest,
      "sent18",
      "Seeding dates ranged from 22 August to 26 September in 2011WS, from 29 February to 1 April in the 2012DS, and from 5 to 23 March in the 2013DS.",
      Seq(
        "DateRange" -> Seq("from 22 August to 26 September in 2011WS", "from 29 February to 1 April", "from 5 to 23 March", "2013DS", "2012DS"),
        "GenericCrop" -> Seq("Seeding"),
        "Planting" -> Seq("Seeding dates"),
      )
    ),
    VariableTest(
      passingTest,
      "sent19",
      "In transplanted rice, seedling age at transplanting was between 26 and 35 days. All farmers grew improved varieties",
      Seq(
        "GenericCrop" -> Seq("varieties"),
        "Crop" -> Seq("rice"),
        "Quantity" -> Seq("between 26 and 35 days"),
        "Planting" -> Seq("transplanted")
      )
    ),
    VariableTest(
      failingTest,
      "sent20",
      "Popular varieties in the wet season were Sahel 202 (65% of farmers) and Sahel 201 (30%), while 60% and 92% grew Sahel 108 in 2012DS and 2013DS, respectively.",
      Seq(
        // FIXME, not an amount! -->  "108 in"
        "Crop" -> Seq("Sahel 108", "Sahel 202", "Sahel 201"),
        "GenericCrop" -> Seq("varieties"),
        "WetSeason" -> Seq("wet season"),
        "DateRange" -> Seq("2012DS","2013DS")
      )
    ),
    VariableTest(
      failingTest,
      "sent21",
      "timing of basal fertilizer application was on average 26, 33, and 26 days after sowing (DAS) in 2011WS, 2012DS, and 2013DS",
      Seq(
        "FertilizerUse" -> Seq("fertilizer application"),
        "Quantity" -> Seq("26, 33 and 26 days"), //FIXME, not all days are extracted.
        "Planting" -> Seq("sowing"),
        "GenericFertilizer" -> Seq("fertilizer"),
        "DateRange" -> Seq("2011WS", "2012DS", "2013DS")
      )
    ),
    VariableTest(
      passingTest,
      "sent22",
      "timing of first top-dressing of N on average for each season was 59–61 DAS",
      Seq(
        "Fertilizer" -> Seq("N")
      )
    ),
    VariableTest(
      passingTest,
      "sent23",
      "WS sowing in July and about 9-10 t ha-1 for dry season (DS) sowing in February in the Senegal River delta.",
      Seq(
        "Quantity" -> Seq("9-10 t ha-1"),
        "Planting" -> Seq("sowing", "sowing"),
        "Location" -> Seq("Senegal River"),
        "DrySeason" -> Seq("dry season"),
        "Date" -> Seq("February", "July")
      )
    ),
    VariableTest(
      passingTest,
      "sent24",
      "Major biophysical constraints included phosphorus (P) deficient soils, weed competition, relatively low nitrogen (N) application rates and sub-optimal timing of N application.",
      Seq(
        "Fertilizer" -> Seq("N", "N", "phosphorus", "P", "nitrogen")
      )
    ),
    VariableTest(
      passingTest,
      "sent25",
      "Most farmers in the Senegal River valley apply N fertilizer only twice, i.e. roughly at the start of tillering and panicle initiation (PI).",
      Seq(
        "Location" -> Seq("Senegal River"),
        "GenericFertilizer" -> Seq("fertilizer"),
        "Fertilizer" -> Seq("N")
      )
    ),
  VariableTest(
    passingTest,
    "sent26",
    "Farmers applied N fertilizer twice, i.e. at the start of tillering and at PI,",
    Seq(
      "GenericFertilizer" -> Seq("fertilizer"),
      "Fertilizer" -> Seq("N")
    )
  ),
    VariableTest(
      failingTest,
      "sent27",
      "Farmers used two rice cultivars (IR1529 and Jaya) during the 1997 WS, and exclusively IR13240-108-2-2-3 (released as Sahel 108 in Senegal in 1994) during the 1998 DS. All rice was direct-seeded.",
      Seq(
        "GenericCrop" -> Seq("cultivars"),
        // `Sahel 108` getting extracted as `Sahel` only.
        "Crop" -> Seq("rice", "Sahel 108", "Jaya", "rice", "IR1529"),
        "Location" -> Seq("Senegal"),
        "Date" -> Seq("1994", "1997"),
        "DateRange" -> Seq("1998 DS", "1997 WS")
      )
    ),
    VariableTest(
      passingTest,
      "sent28",
      "Average WS T0 yield was high, i.e. 7.3 t ha-1 (ranging from 5.0 to 9.4 t ha-1), considering the region’s potential yield of about 9 t ha-1.",
      Seq(
        "Yield" -> Seq("yield", "yield"),
        "Quantity" -> Seq("from 5.0 to 9.4 t ha-1", "9 t ha-1", "7.3 t ha-1"),
      )
    ),
    VariableTest(
      passingTest,
      "sent29",
      "Average DS T0 yield was relatively low, i.e. 4.4 t ha-1 (ranging from 2.5 to 6.0 t ha-1)",
      Seq(
        "Yield" -> Seq("yield"),
        "Quantity" -> Seq("4.4 t ha-1", "from 2.5 to 6.0 t ha-1"),
        "AreaSize" -> Seq.empty
      )
    ),
    VariableTest(
      failingTest,
      "sent30",
      "The favourable climate conditions and the adoption of varieties with shorter cropping cycles allow for two rice harvests per year, namel in the dry and rainy seasons (Van Oort et al., 2016).",
      Seq(
        "GenericCrop" -> Seq("varieties", "cropping"),
        //FIXME; seasons separated by 'and' extracted as one entity.
        "DrySeason" -> Seq("dry seasons"),
        "WetSeason" -> Seq("rainy seasons"),
        "Crop" -> Seq("rice"),
        "Date" -> Seq("2016")
      )
    ),
    VariableTest(
      passingTest,
      "sent31",
      "In the SRV, production areas are typically larger in the dry season, which brings fewer problems with pests and birds (Tanaka et al., 2015; USDA-GAIN, 2018).",
      Seq(
        "DrySeason" -> Seq("dry season"),
        "Date" -> Seq("2015", "2018")
      )
    ),
    VariableTest(
      passingTest,
      "sent32",
      "Average yields in SRV theoretically range between 5.0 and 6.0 t ha-1 in the rainy season and between 6.5 and 7.5 t ha-1 in the dry season (SAED, 2019; USDA-GAIN, 2021)",
      Seq(
        "Yield" -> Seq("yields"),
        "DrySeason" -> Seq("dry season"),
        "Date" -> Seq("2021", "2019"),
        "Quantity" -> Seq("between 6.5 and 7.5 t ha-1", "between 5.0 and 6.0 t ha-1")
      )
    ),
    VariableTest(
      passingTest,
      "sent33",
      "seeds of the rice variety Sahel 108 are sown, a short- cycle variety (around 125 days)",
      Seq(
        "Quantity" -> Seq("125 days"),
        "Planting" -> Seq("sown"),
        "GenericCrop" -> Seq("variety", "variety", "seeds"),
        "Crop" -> Seq("rice", "Sahel 108")
      )
    ),
    VariableTest(
      passingTest,
      "sent34",
      "Broadcast seeding is carried out by hand on irrigated plots with a 2-5 cm depth sheet of water",
      Seq(
        "Quantity" -> Seq("2-5 cm"),
        "Planting" -> Seq("seeding"),
        "GenericCrop" -> Seq("seeding")
      )
    ),
    VariableTest(
      passingTest,
      "sent35",
      "only two top dressings are applied in the CONV scenario ( the first one with urea and dia- mmonium phosphate (DAP) at the beginning of tillering; and the second only with urea at panicle initiation.",
      Seq(
        "Fertilizer" -> Seq("urea", "DAP", "phosphate", "urea"),
      )
    ),
    VariableTest(
      passingTest,
      "sent36",
      "In INT, a basic dressing is firstly applied with urea and DAP, followed by two top-dressing applications with urea.",
      Seq(
        "Fertilizer" -> Seq("urea", "DAP", "urea"),
      )
    ),

    VariableTest(
      passingTest,
      "sent37",
      "average yields for the two seasons assessed of 4832 kg ha-1 and 7425 kg ha-1 for the areas under CONV and INT management, respectively.",
      Seq(
        "Yield" -> Seq("yields"),
        "Quantity" -> Seq("4832 kg ha-1", "7425 kg ha-1")
      )
    ),
    VariableTest(
      passingTest,
      "sent38",
      "Agricultural inputs, machinery operations and yields considered in the four scenarios evaluated: conventional (CONV), intensive (INT), and the two refernce scenarios SAED_2td and SAED_3td. DAP: Diammonium phosphate.",
      Seq(
        "FertilizerUse" -> Seq("inputs"),
        "Yield" -> Seq("yields"),
        "Fertilizer" -> Seq("DAP", "Diammonium phosphate")
      )
    ),
    VariableTest(
      passingTest,
      "sent39",
      "Fertilisers considered and associated nutrients in the four scenarios evaluated: conventional (CONV), intensive (INT), and the two reference scenarios SAED_2td and SAED_3td. DAP: diammonium phosphate.",
      Seq(
        "Fertilizer" -> Seq("DAP", "diammonium phosphate")
      )
    ),
    VariableTest(
      failingTest,
      "sent40",
      "In plots receiving fertilizer, DAP was applied basally (19.3 and 21.5 kg N and P ha-1 ), and three urea splits were broadcasted into 1–5 cm of water (101.3 kg N ha-1 ; 40% at early-tillering, 40% at panicle initiation, and 20% at heading)",
      Seq(
        "Fertilizer" -> Seq("DAP", "N", "P", "urea", "N"),
        "Quantity" -> Seq("101.3 kg", "1–5 cm", "21.5 kg"), //fixme: should include 19.3
        "GenericFertilizer" -> Seq("fertilizer"),
        "FertilizerUse" -> Seq("fertilizer"),
        "Fertilizer" -> Seq("N", "urea", "P", "N", "DAP")
      )
    ),
    VariableTest(
      passingTest,
      "sent41",
      "The short-duration O. sativa cultivar Sahel-108 (IR-13240-108- 2-2-3) was grown during all seasons.",
      Seq(
        "GenericCrop" -> Seq("cultivar"),
        "Crop" -> Seq("Sahel-108")
      )
    ),
    VariableTest(
      passingTest,
      "sent42",
      "Transplanting for both management systems took place on 7 (2007 and 2008 wet season) and 25 (2009 wet season) August and on 19 March (2008 and 2009 dry season).",
      Seq(
        "Date" -> Seq("2007", "2008", "2009", "August", "19 March", "2008", "2009"),
        "DrySeason" -> Seq("dry season"),
        "Planting" -> Seq("Transplanting"),
        "WetSeason" -> Seq("wet season", "wet season")
      )
    ),
    VariableTest(
      passingTest,
      "sent43",
      "Year, site, crop management, soil properties, and rainfall distribution in the experiments in Senegal used for DSSAT calibration and validation.",
      Seq(
        "Location" -> Seq("Senegal"),
        "GenericCrop" -> Seq("crop")
      )
    ),
    VariableTest(
      failingTest,
      "sent44",
      "das days after sowing, Fert fertilizer treatment, with F1: recommended dose (80 kg N ha-1), i.e., 200 kg ha-1 NPK (15.15.15) at sowing + 100 kg ha-1 urea at 20 das + 50 kg ha-1 urea at 50 das. F2: F1/4 (20 kg N ha-1);",
      Seq(
        "Fertilizer" -> Seq("N", "NPK", "urea", "urea" ),
        "FertilizerUse" -> Seq("fertilizer"),
        "GenericFertilizer" -> Seq("fertilizer"),
        "Planting" -> Seq("sowing", "sowing"),
        "Quantity" -> Seq("80 kg","20k", "200 kg", "100 kg ha-1", "50 kg ha-1")
        // fixme: need measurement adjustment in processors; some measurement issues here.
      )
    ),

    // Following the text extraction analyses.
    VariableTest(
    failingTest,
    "Fix1",
      // FIXME; yield range not extracted
    "Rice grain yield measured at maturity ranged from 2.7 t ha-1 to 7.1 t ha-1 , with an average of 4.8 t ha-1 .",
      Seq(
        "Quantity" -> Seq("from 2.7 t ha-1 to 7.1 t ha-1", "4.8 t ha-1")
      )
    ),
    VariableTest(
      failingTest,
      "Fix2",
      // FIXME; 9.2 t ha-1 getting missed.
      "Diagnosis of the 1999 and 2000 wet seasons In the 1999 and 2000 wet seasons , the potential rice grain yields were between 8.8 t ha-1 and 9.2 t ha-1 ( i.e. about 1 t ha-1 more than in the 1998WS ) whilst the average of the actual yield increased greatly ( Tab .",
      Seq(
        "Quantity" -> Seq("between 8.8 t ha-1 and 9.2 t ha-1", "1 t ha-1")
      )
    ),
    VariableTest(
      failingTest,
      "Fix3",
      "The highest yield ( 9.3 t ha-1 ) is obtained by Brodt et al. ( 2014 ) in California with only 170 kg N ha1 ; followed by Xu et al. ( 2020 ) and Zhang et al. ( 2021 ) , both with yields > 8 t ha-1 in Hubei Province ( China ) .",
      Seq(
        "Quantity" -> Seq("9.3 t ha-1", "8 t ha-1")
        // FIXME; Will check this out later, found that `8 t ha-1` getting extracted as fertilizerQuantity inn event test..
      )
    ),
    VariableTest(
      failingTest,
      "Fix4",
      // FIXME; the range 9 and 10  not captured, but 10 t/ha and 8 t/ha get extracted.
      "Potential yield of all the varieties in the Senegal River delta was estimated at 9 and 10 t / ha in wet and dry seasons , respectively , and potential yield was taken as 8 t / ha for both seasons in the middle valley .",
      Seq(
        "Quantity" -> Seq("9 and 10 t / ha", "8 t / ha")
      )
    ),
    VariableTest(
      failingTest,
      "Fix5",
      // FIXME; maybe wrong parsing here and hence getting wrong extraction.
      "Target yields on average were set to 6.4 , 7.9 , and 7.1 t / ha in 2011WS , 2012DS , and 2013DS , respectively ( Table 1 ) .",
      Seq(
        "Quantity" -> Seq("6.4 , 7.9 , and 7.1 t / ha")
      )
    ),
    VariableTest(
      failingTest,
      "Fix6",
      // FIXME; Not so sure what text to put for this for extraction; 3600 does not extract.
      "With RCP2.6 and consideration of CO2 effect , rice yield will increase from 3600 in 2000-2009 to 4500 kg ha-1 in 2090-2099 ( Fig. 4a ) .",
      Seq(
        "Quantity" -> Seq("from 3600 in 2000-2009 to 4500 kg ha-1")
      )
    ),
    VariableTest(
      failingTest,
      "Fix7",
      "With ME and BC climate models , the crop model simulated an increase in yield from 2700-2800 in 2000s to 3200-3500 kg ha-1in the 2050s , while it predicted a large decrease ( below 2000 kg ha-1 ) with the other climate models .",
      Seq(
        "Quantity" -> Seq("2700-2800 kg ha-1", "3200-3500 kg ha-1")
      )
    ),
    VariableTest(
      passingTest,
      "Fix8",
      "Rice cultivation and management are autosufficient in terms of seed supply , irrigation and rice grain milling .",
      Seq(
        "Crop" -> Seq("Rice", "rice")
      )
    ),
    VariableTest(
      failingTest,
      "Fix9",
      // FIXME; would explain.
      "The 21 cultivars used in the experiment were either hybrids , japonica , or indica type and came from various breeding centers ( Table 2 ) .",
      Seq(
        "Crop" -> Seq("japonica", "indica")
      )
    ),



  )


  variableTests.zipWithIndex.foreach { case (variableTest, index) => variableTest.test(index) }
}


