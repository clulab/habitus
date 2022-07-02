package org.clulab.habitus.variables

import org.clulab.habitus.utils.Test
import org.clulab.odin.Mention

import scala.collection.mutable.ArrayBuffer

class TestEntitiesNorms extends Test {
  // checks entity mentions:
  // the variable argument should be a text bound mention of the correct label
  // the value argument should have the correct expected extraction.
  // Here, we only test entities that have norms (numeric entities)
  val vp: VariableProcessor = VariableProcessor()

  case class VariableTest(
                           shouldable: Shouldable,
                           name: String,
                           // Text for extraction
                           text: String,
                           // Expected label extraction; a text can have multiple extractions for multiple labels.
                           desired:  Seq[(String, Seq[(String, String)])]
                           // Expected values from text extraction.
                         ) {

    def getMentions(text: String): Seq[Mention] = {
      val parsingResults = vp.parse(text)
      // in this tester, we want to get all mentions, including entity ones, so use parsingResults.allMentions
      // instead of parsingResults.targetMentions
      parsingResults.allMentions
    }




    def test(index: Int): Unit = {
      shouldable should s"filter by correct label(s) and value size of sent $name correctly" in {
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

          // Get the norms
          val targetMentionNorms = targetMentions.map(_.norms.get.head) // text of similar kinds but for norms(

          // Get the text
          val targetMentionText = targetMentions.map(_.text)

          // Zip extracted text with its norm
          val textNormCouple = targetMentionText zip targetMentionNorms

          //check if the required texts and norms were extracted
          values.foreach(couple => textNormCouple should contain(couple))

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
      Seq("Quantity" -> Seq(("5 t ha-1","5.0 t/ha")) )
    ),
    VariableTest(
      passingTest,
      "sent2",
      "The potential grain yield that can be obtained ranges from 8 to 9 t ha-1 in the wet season (July sowing) and from 6 to 11 t ha-1 in the dry season (February sowing)",
      Seq("Date"-> Seq(("July", "XXXX-07-XX"), ("February", "XXXX-02-XX")),
        "Quantity" -> Seq(("from 6 to 11 t ha-1","6.0 -- 11.0 t/ha"), ("from 8 to 9 t ha-1","8.0 -- 9.0 t/ha")),
      )
    ),

    VariableTest(
      passingTest,
      "sent3",
      "The potential yields of these three cultivars are similar and are on average about 8 to 9 t ha-1 in the wet season",
      Seq(
        "Quantity" -> Seq(("8 to 9 t ha-1","8.0 -- 9.0 t/ha"))
      )
    ),
    VariableTest(
      passingTest,
      "sent4",
      "in the 1999WS, with an average grain yield of 7.2 t ha-1. In the 2000WS",
      Seq(
        "Quantity" -> Seq(("7.2 t ha-1", "7.2 t/ha")),
      )
    ),
    VariableTest(
      passingTest,
      "sent5",
      "the average grain yield was 8.2 t ha-1",
      Seq(
        "Quantity" -> Seq(("8.2 t ha-1", "8.2 t/ha")),
      )
    ),
    VariableTest(
      passingTest,
      "sent6",
      "Average yield reached 7.2 t ha-1 in 1999 and 8.2 t ha-1 in 2000",
      Seq(
        "Quantity" -> Seq(("7.2 t ha-1", "7.2 t/ha"), ("8.2 t ha-1", "8.2 t/ha"))
      )
    ),
    VariableTest(
      passingTest,
      "sent7",
      "Potential rice grain yields (limited by solar radiation and temperature only) are on average about 9 t ha-1 in the wet growing season from July to November",
      Seq(
        "Quantity" -> Seq(("9 t ha-1", "9.0 t/ha")),
        "DateRange" -> Seq(("from July to November", "XXXX-07-XX -- XXXX-11-XX")),
      )
    ),
    VariableTest(
      passingTest,
      "sent8",
      "These correspond to the dry season (from February/March to June/July).",
      Seq(
        "DateRange" -> Seq(("from February/March to June/July", "XXXX-02-XX -- XXXX-07-XX"))
      )
    ),
    VariableTest(
      passingTest,
      "sent9",
      "Farmers’ yields are on average between 4 and 5 t ha-1, and, therefore, far below potential.",
      Seq(
        "Quantity" -> Seq(("between 4 and 5 t ha-1", "4.0 -- 5.0 t/ha"))
      )
    ),
    VariableTest(
      passingTest,
      "sent10",
      "recommended N rates by Société Nationale d’Aménagement et d’Exploitation des Terres du Delta du fleuve Sénégal et des Vallées du fleuve Sénégal (SAED) range from 133 to 179 kg/ha, applied as di-ammonium phosphate (DAP) at around sowing or transplanting, and three splits of urea (at early tillering, panicle initiation, and booting)",
      Seq(
        "Quantity" -> Seq(("from 133 to 179 kg/ha", "133.0 -- 179.0 kg/ha")),
      )
    ),
    VariableTest(
      passingTest,
      "sent11",
      "The recommended rate is higher in the dry season (Haefele and Wopereis, 2004), and higher in the Senegal River delta than in the middle valley, where extreme tem- peratures tend to occur and affect potential yield.",
      Seq(
        "Date" -> Seq(("2004", "2004-XX-XX"))
      )
    ),
    VariableTest(
      fixedWithNewProcRelease,
      "sent12",
      "Seeding dates ranged from 22 August to 26 September in 2011WS, from 29 February to 1 April in the 2012DS, and from 5 to 23 March in the 2013DS.",
      Seq(
        "DateRange" -> Seq(
          ("from 22 August to 26 September in 2011WS","2011-08-22 -- 2011-09-26"),
          ("from 29 February to 1 April in the 2012DS", "2012-02-29 -- 2012-04-01"),
          ("from 5 to 23 March in the 2013DS", "2013-03-05 -- 2013-03-23")
        )
      )
    ),
    VariableTest(
      passingTest,
      "sent13",
      "In transplanted rice, seedling age at transplanting was between 26 and 35 days. All farmers grew improved varieties",
      Seq(
        "Quantity" -> Seq(("between 26 and 35 days", "26.0 -- 35.0 d")),
      )
    ),
    VariableTest(
      fixedWithNewProcRelease,
      "sent14",
      "timing of basal fertilizer application was on average 26, 33, and 26 days after sowing (DAS) in 2011WS, 2012DS, and 2013DS",
      Seq(
        "Quantity" -> Seq(("26", "26.0 d"), ("33", "33.0 d"), ("26 days", "26.0 d"))
      )
    ),
    VariableTest(
      passingTest,
      "sent15",
      "WS sowing in July and about 9-10 t ha-1 for dry season (DS) sowing in February in the Senegal River delta.",
      Seq(
        "Quantity" -> Seq(("9-10 t ha-1","9.0 -- 10.0 t/ha")),
        "Date" -> Seq(("February", "XXXX-02-XX"), ("July", "XXXX-07-XX")),
      )
    ),
    VariableTest(
      passingTest,
      "sent16",
      "Farmers used two rice cultivars (IR1529 and Jaya) during the 1997 WS, and exclusively IR13240-108-2-2-3 (released as Sahel 108 in Senegal in 1994) during the 1998 DS. All rice was direct-seeded.",
      Seq(
        "Date" -> Seq(("1994", "1994-XX-XX")),
        "DateRange" -> Seq( ("1997 WS", "1997-XX-XX -- 1997-XX-XX"),("1998 DS", "1998-XX-XX -- 1998-XX-XX") )
      )
    ),
    VariableTest(
      passingTest,
      "sent17",
      "Average WS T0 yield was high, i.e. 7.3 t ha-1 (ranging from 5.0 to 9.4 t ha-1), considering the region’s potential yield of about 9 t ha-1.",
      Seq(
        "Quantity" -> Seq(("9 t ha-1", "9.0 t/ha"), ("from 5.0 to 9.4 t ha-1","5.0 -- 9.4 t/ha"), ("7.3 t ha-1", "7.3 t/ha")),
      )
    ),
    VariableTest(
      passingTest,
      "sent18",
      "Average DS T0 yield was relatively low, i.e. 4.4 t ha-1 (ranging from 2.5 to 6.0 t ha-1)",
      Seq(
        "Quantity" -> Seq(("4.4 t ha-1", "4.4 t/ha"), ("from 2.5 to 6.0 t ha-1", "2.5 -- 6.0 t/ha"))
      )
    ),
    VariableTest(
      passingTest,
      "sent19",
      "The favourable climate conditions and the adoption of varieties with shorter cropping cycles allow for two rice harvests per year, namel in the dry and rainy seasons (Van Oort et al., 2016).",
      Seq(
        "Date" -> Seq(("2016", "2016-XX-XX"))
      )
    ),
    VariableTest(
      passingTest,
      "sent20",
      "In the SRV, production areas are typically larger in the dry season, which brings fewer problems with pests and birds (Tanaka et al., 2015; USDA-GAIN, 2018).",
      Seq(
        "Date" -> Seq(("2015", "2015-XX-XX"), ("2018","2018-XX-XX"))
      )
    ),
    VariableTest(
      passingTest,
      "sent21",
      "Average yields in SRV theoretically range between 5.0 and 6.0 t ha-1 in the rainy season and between 6.5 and 7.5 t ha-1 in the dry season (SAED, 2019; USDA-GAIN, 2021)",
      Seq(
        "Date" -> Seq(("2021", "2021-XX-XX"), ("2019", "2019-XX-XX")),
        "Quantity" -> Seq(("between 6.5 and 7.5 t ha-1", "6.5 -- 7.5 t/ha"), ("between 5.0 and 6.0 t ha-1", "5.0 -- 6.0 t/ha"))
      )
    ),
    VariableTest(
      passingTest,
      "sent22",
      "seeds of the rice variety Sahel 108 are sown, a short- cycle variety (around 125 days)",
      Seq(
        "Quantity" -> Seq(("125 days", "125.0 d")),
      )
    ),
    VariableTest(
      passingTest,
      "sent23",
      "Broadcast seeding is carried out by hand on irrigated plots with a 2-5 cm depth sheet of water",
      Seq(
        "Quantity" -> Seq(("2-5 cm", "2.0 -- 5.0 cm")),
      )
    ),
    VariableTest(
      passingTest,
      "sent24",
      "average yields for the two seasons assessed of 4832 kg ha-1 and 7425 kg ha-1 for the areas under CONV and INT management, respectively.",
      Seq(
        "Quantity" -> Seq(("4832 kg ha-1", "4832.0 kg/ha"), ("7425 kg ha-1","7425.0 kg/ha"))
      )
    ),
    VariableTest(
      failingTest,
      "sent25",
      "In plots receiving fertilizer, DAP was applied basally (19.3 and 21.5 kg N and P ha-1 ), and three urea splits were broadcasted into 1-5 cm of water (101.3 kg N ha-1 ; 40% at early-tillering, 40% at panicle initiation, and 20% at heading)",
      Seq(
        // FIXME; `19.3 and 21.5 kg` not extracted fully (without 19.3). Also some wrong Quantity extractions. Discuss with Masha.
        "Quantity" -> Seq(
          ("101.3 kg", "101.3 kg"),
          ("1-5 cm", "1.0 -- 5.0 cm"),
          ("21.5 kg", "21.5 kg")
        )
      )
    ),
    VariableTest(
      passingTest,
      "sent26",
      "Transplanting for both management systems took place on 7 (2007 and 2008 wet season) and 25 (2009 wet season) August and on 19 March (2008 and 2009 dry season).",
      Seq(
        "Date" -> Seq(("2007", "2007-XX-XX"), ("2008", "2008-XX-XX"), ("2009", "2009-XX-XX"),
                        ("August", "XXXX-08-XX"), ("19 March", "XXXX-03-19"), ("2008", "2008-XX-XX"), ("2009", "2009-XX-XX")),
      )
    ),
    VariableTest(
      fixedWithNewProcRelease,
      "sent27",
      "das days after sowing, Fert fertilizer treatment, with F1: recommended dose (80 kg N ha-1), i.e., 200 kg ha-1 NPK (15.15.15) at sowing + 100 kg ha-1 urea at 20 das + 50 kg ha-1 urea at 50 das. F2: F1/4 (20 kg N ha-1); C/V/P calibration/validation/projection.",
      Seq(
        "Quantity" -> Seq(
          ("80 kg N ha-1", "80.0 kg n ha-1"),
          ("200 kg", "200.0 kg"), // fixme: not currently solvable---handling a crucial case in processors resulted in the loss of ha-1 here
          ("+ 100 kg ha-1", "100.0 kg/ha"),
          ("+ 50 kg ha-1", "50.0 kg/ha"),
          ("20 kg N ha-1", "20.0 kg n ha-1")
        )
      )
    ),
    VariableTest(
      failingTest,
      "sent28",
      "Soil properties before the experiment were 0.87 g total N kg-1 and 9.6 g total C kg-1 ( analysis with a Vario Max Dry Combustion Analyzer , Elementar Americas , Mt. Laurel , NJ following the IOS ( 1998 ) ) , 5.81 kg P-Olsen kg-1 ( measured following extraction in 0.5 M NaHCO3 at pH 8.5 ( Olsen et al ., 1954 ) ) , 0.47 cmol exchangeable K kg-1 ( atomic absorption spectroscopy after extraction in 1 M NH4OAc at pH 7 ) , pH of 4.76 ( 1:2.5 H20 saturated paste ) and EC 0.33 dS m-1 ( 1:5 H20 saturated paste ) .",
      Seq(
        "Quantity" -> Seq(
          ("0.87 g N kg-1", "0.87 g/kg"),
          ("9.6 g C kg-1", "9.6 g/kg"),
          ("5.81 kg P-Olsen kg-1", "5.81 kg/kg")
        )
      )
    ),
    VariableTest(
      fixedWithNewProcRelease,
      "sent29",
      "Rice grain yield measured at maturity ranged from 2.7 t ha-1 to 7.1 t ha-1 , with an average of 4.8 t ha-1 .",
      Seq(
        "Quantity" -> Seq(
          ("from 2.7 t ha-1 to 7.1 t ha-1", "2.7 -- 7.1 t/ha"),
          ("4.8 t ha-1", "4.8 t/ha"))
      )
    ),
    // Following the text extraction analyses.
    VariableTest(
      fixedWithNewProcRelease,
      "Fix2",
      "Diagnosis of the 1999 and 2000 wet seasons In the 1999 and 2000 wet seasons , the potential rice grain yields were between 8.8 t ha-1 and 9.2 t ha-1 ( i.e. about 1 t ha-1 more than in the 1998WS ) whilst the average of the actual yield increased greatly ( Tab .",
      Seq(
        "Quantity" -> Seq(
          ("between 8.8 t ha-1 and 9.2 t ha-1", "8.8 -- 9.2 t/ha"),
          ("1 t ha-1", "1.0 t/ha")
        )
      )
    ),
    VariableTest(
      fixedWithNewProcRelease,
      "Fix3",
      "The highest yield ( 9.3 t ha-1 ) is obtained by Brodt et al. ( 2014 ) in California with only 170 kg N ha-1 ; followed by Xu et al. ( 2020 ) and Zhang et al. ( 2021 ) , both with yields > 8 t ha-1 in Hubei Province ( China ) .",
      Seq(
        "Quantity" -> Seq(
          ("9.3 t ha-1", "9.3 t/ha"),
          ("170 kg N ha-1", "170.0 kg n ha-1"), // fixme: check processors for why the norm is like this
          ("8 t ha-1", "8.0 t/ha")
        )
      )
    ),
    VariableTest(
      fixedWithNewProcRelease,
      "Fix4",
      "Potential yield of all the varieties in the Senegal River delta was estimated at 9 and 10 t/ha in wet and dry seasons , respectively , and potential yield was taken as 8 t/ha for both seasons in the middle valley .",
      Seq(
        "Quantity" -> Seq(
          ("9", "9.0 t/ha"),
          ("10 t/ha", "10.0 t/ha"),
          ("8 t/ha", "8.0 t/ha")
        )
      )
    ),
    VariableTest(
      fixedWithNewProcRelease,
      "Fix5",
      // FIXME; maybe wrong parsing here and hence getting wrong extraction.
      "Target yields on average were set to 6.4, 7.9, and 7.1 t/ha in 2011WS , 2012DS , and 2013DS , respectively ( Table 1 ) .",
      Seq(
        "Quantity" -> Seq(
          ("6.4", "6.4 t/ha"),
          ("7.9", "7.9 t/ha"),
          ("7.1 t/ha", "7.1 t/ha")
        ),
        "DateRange" -> Seq(
          ("2011WS", "2011-XX-XX -- 2011-XX-XX"),
          ("2012DS", "2012-XX-XX -- 2012-XX-XX"),
          ("2013DS", "2013-XX-XX -- 2013-XX-XX")
        )
      )
    ),
    VariableTest(
      fixedWithNewProcRelease,
      "Fix6",
      "With RCP2.6 and consideration of CO2 effect , rice yield will increase from 3600 in 2000-2009 to 4500 kg ha-1 in 2090-2099 ( Fig. 4a ) .",
      Seq(
        "Quantity" -> Seq(
          ("3600", "3600.0 kg/ha"),
          ("4500 kg ha-1", "4500.0 kg/ha")
        ),
        "DateRange" -> Seq(
          ("2000-2009", "2000-XX-XX -- 2009-XX-XX"),
          ("2090-2099", "2090-XX-XX -- 2099-XX-XX")
        )
      )
    ),
    VariableTest(
      failingTest,
      "Fix7",
      "With ME and BC climate models , the crop model simulated an increase in yield from 2700-2800 in 2000s to 3200-3500 kg ha-1 in the 2050s , while it predicted a large decrease ( below 2000 kg ha-1 ) with the other climate models .",
      Seq(
        "Quantity" -> Seq(
          ("2700-2800", "2700.0 -- 2800.0 kg/ha"), // fixme: issue in processors: 2700-2800 is extracted as a year
          ("3200-3500 kg ha-1", "3200 -- 2500 kg/ha"),
          ("2000 kg ha-1", "2000.0 kg/ha")
          )
        )
    ),
    VariableTest(
      passingTest,
      "Fix8", // 2009 is the year of publication
      "In another study in the same location , RI yields as high as 9.9 t ha-1 were recorded , though rather than ttributing them to SRI per se , Tsujimoto et al. ( 2009 ) suggested hey resulted from repetitive , deep tillage and incorporation of up o 35 t compost ha-1 ( dry weight equivalent ).",
      Seq(
        "Date" -> Seq.empty
      )
    )
  )


  variableTests.zipWithIndex.foreach { case (variableTest, index) => variableTest.test(index) }
}


