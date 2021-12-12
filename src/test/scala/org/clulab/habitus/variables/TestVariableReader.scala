package org.clulab.habitus.variables

import org.clulab.odin.{ExtractorEngine, Mention}
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.fastnlp.FastNLPProcessor
import org.scalatest.{FlatSpec, Matchers}

//
// TODO: write tests for all sentences, similar to this: https://github.com/clulab/reach/blob/master/main/src/test/scala/org/clulab/reach/TestActivationEvents.scala
//

class TestVariableReader extends FlatSpec with Matchers {
  val vp = VariableProcessor()

  def getMentions(text: String): Seq[Mention] = {
    val (_, mentions,_,_) = vp.parse(text)
    mentions              
  }

  // the Clu parser breaks on this one, but the SRL works fine!
  val sent1 = "Farmers’ sowing dates ranged from 14 to 31 July for the WS and from 3 to 11 March for the DS."
  sent1 should "recognize range"  in {
    val mentions = getMentions(sent1)
    mentions.filter(_.label matches "Assignment") should have size (2)
    var count = 0
    for (m <- mentions.filter(_.label matches "Assignment")) {
      if (count == 0) {
        m.arguments("variable").head.text should be ("sowing dates")
        m.arguments("value").head.text should equal ("from 3 to 11 March")
        m.arguments("value").head.norms.get(0) should equal("XXXX-03-03 -- XXXX-03-11")
      } else {
        m.arguments("variable").head.text should be ("sowing dates")
        m.arguments("value").head.text should equal ("from 14 to 31 July")
        m.arguments("value").head.norms.get(0) should equal("XXXX-07-14 -- XXXX-07-31")
      }
      count += 1
    }
  }

  val sent2 = "Sowing between October 4 and October 14 was optimal."
  sent2 should "recognize sowing date with range Month followed by day"  in {
    val mentions = getMentions(sent2)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Sowing")
      m.arguments("value").head.text should equal("between October 4 and October 14")
      m.arguments("value").head.norms.get(0) should equal("XXXX-10-04 -- XXXX-10-14")
    })
  }

  val sent3 = "Sowing date was October 7, 2019 ."
  sent3 should "recognize sowing date was a Month followed by day, year"  in {
    val mentions = getMentions(sent3)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Sowing date")
      m.arguments("value").head.text should equal("October 7, 2019")
      m.arguments("value").head.norms.get(0) should equal("2019-10-07")
    })
  }

  // //TODO: ms of year do not captured by our numbers.
  // val sent4 = "Sowing between October 4 and October 14 of 2020 was optimal."
  // sent4 should "recognize sowing date with range Month followed by 4 of year"  in {
  //   val mentions = getMentions(sent4)
  //   mentions.filter(_.label matches "Assignment") should have size (1)
  //   mentions.filter(_.label matches "Assignment").foreach({ m =>
  //     m.arguments("variable").head.text should be("Sowing")
  //     m.arguments("value").head.text should equal("between October 4 and October 14 of 2020")
  //     m.arguments("value").head.norms.get(0) should equal("2020-10-04 -- 2020-10-14")
  //   })
  // }

  val sent5 = "Planting date was October 1."
  sent5 should "recognize sowing date with a date consisting a month followed by 4"  in {
    val mentions = getMentions(sent5)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Planting date")
      m.arguments("value").head.text should equal("October 1")
      m.arguments("value").head.norms.get(0) should equal("XXXX-10-01")
    })
  }

  // //TODO: ms of year do not captured by our numbers.
  // val sent6 = "Planting date was October 1 of 2021."
  // sent6 should "recognize sowing date with a date consisting a month followed by day then [of a year]"  in {
  //   val mentions = getMentions(sent6)
  //   mentions.filter(_.label matches "Assignment") should have size (1)
  //   mentions.filter(_.label matches "Assignment").foreach({ m =>
  //     m.arguments("variable").head.text should be("Planting date")
  //     m.arguments("value").head.text should equal("October 1 of 2021")
  //     m.arguments("value").head.norms.get(0) should equal("2021-10-01")
  //   })
  // }

  val sent7 = "Planting occurred on October 2."
  sent7 should "recognize sowing occurred on with a date consisting a month followed by 4"  in {
    val mentions = getMentions(sent7)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Planting")
      m.arguments("value").head.text should equal("October 2")
      m.arguments("value").head.norms.get(0) should equal("XXXX-10-02")
    })
  }

  val sent8 = "Planting date was October 4, 2019"
  sent8 should "recognize planting date was a date consisting a month followed by date"  in {
    val mentions = getMentions(sent8)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Planting date")
      m.arguments("value").head.text should equal("October 4, 2019")
      m.arguments("value").head.norms.get(0) should equal("2019-10-04")
    })
  }

  val sent9 = "Planting occurred on October 1, 2019."
  sent9 should "recognize planting occurred on a date consisting a month followed by a date, year"  in {
    val mentions = getMentions(sent9)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Planting")
      m.arguments("value").head.text should equal("October 1, 2019")
      m.arguments("value").head.norms.get(0) should equal("2019-10-01")
    })
  }

  val sent10 = "Sowing occurred on October 1, 2019"
  sent10 should "recognize sowing occurred on a Month followed by day, year"  in {
    val mentions = getMentions(sent10)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Sowing")
      m.arguments("value").head.text should equal("October 1, 2019")
      m.arguments("value").head.norms.get(0) should equal("2019-10-01")
    })
  }

  val sent10_2 = "Seeding occurred on October 1, 2019"
  sent10_2 should "recognize sowing occurred on a Month followed by day, year"  in {
    val mentions = getMentions(sent10_2)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Seeding")
      m.arguments("value").head.text should equal("October 1, 2019")
      m.arguments("value").head.norms.get(0) should equal("2019-10-01")
    })
  }

  val sent11 = "Sowing on October 9, 2019."
  sent11 should "recognize sowing on [no verb attached] a Month followed by day, year"  in {
    val mentions = getMentions(sent11)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Sowing")
      m.arguments("value").head.text should equal("October 9, 2019")
      m.arguments("value").head.norms.get(0) should equal("2019-10-09")
    })
  }

  val sent11_2 = "Planting on April 7, 2019"
  sent11_2 should "recognize sowing on [no verb attached] a Month followed by day, year"  in {
    val mentions = getMentions(sent11_2)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Planting")
      m.arguments("value").head.text should equal("April 7, 2019")
      m.arguments("value").head.norms.get(0) should equal("2019-04-07")
    })
  }

  // //TODO: ms of year do not captured by our numbers.
  // val sent12 = "Sowing between October 4 and October 14 of 2020 was optimal."
  // sent12 should "recognize sowing range on a Month followed by day of year"  in {
  //   val mentions = getMentions(sent12)
  //   mentions.filter(_.label matches "Assignment") should have size (1)
  //   mentions.filter(_.label matches "Assignment").foreach({ m =>
  //     m.arguments("variable").head.text should be("Sowing")
  //     m.arguments("value").head.text should equal("between October 4 and October 14 of 2020")
  //     m.arguments("value").head.norms.get(0) should equal("2020-10-04 -- 2020-10-14")
  //   })
  // }

  // val sent12_1 = "Sowing dates between October 4 and October 14, 2020 was optimal."
  // sent12_1 should "recognize sowing range on a Month followed by day of year"  in {
  //   val mentions = getMentions(sent12_1)
  //   mentions.filter(_.label matches "Assignment") should have size (1)
  //   mentions.filter(_.label matches "Assignment").foreach({ m =>
  //     m.arguments("variable").head.text should be("Sowing dates")
  //     m.arguments("value").head.text should equal("between October 4 and October 14, 2020")
  //     m.arguments("value").head.norms.get(0) should equal("2020-10-04 -- 2020-10-14")
  //   })
  // }

  val sent13 = "Sowing in May, 2019."
  sent13 should "recognize sowing on [no verb attached] a Month followed by day, year"  in {
    val mentions = getMentions(sent13)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Sowing")
      m.arguments("value").head.text should equal("May, 2019")
      m.arguments("value").head.norms.get(0) should equal("2019-05-XX")
    })
  }

  val sent14 = "Sowing in April, 2011 could increase outputs."
  sent14 should "recognize sowing on [no verb attached] a Month followed by day, year"  in {
    val mentions = getMentions(sent14)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Sowing")
      m.arguments("value").head.text should equal("April, 2011")
      m.arguments("value").head.norms.get(0) should equal("2011-04-XX")
    })
  }

  val sent14_1 = "Planting in April, 2011 is recommended for the year."
  sent14_1 should "recognize sowing on [no verb attached] a Month followed by day, year"  in {
    val mentions = getMentions(sent14_1)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Planting")
      m.arguments("value").head.text should equal("April, 2011")
      m.arguments("value").head.norms.get(0) should equal("2011-04-XX")
    })
  }

  val sent15 = "Sowing on September 8 yields best outcomes."
  sent15 should "recognize sowing on [no verb attached] a Month followed by day, year"  in {
    val mentions = getMentions(sent15)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Sowing")
      m.arguments("value").head.text should equal("September 8")
      m.arguments("value").head.norms.get(0) should equal("XXXX-09-08")
    })
  }

  //TODO: ms, but without [, year] it does not complain.
  val sent15_2 = "Sowing on April 8 yields best outcomes."
  sent15_2 should "recognize sowing on [no verb attached] a Month followed by day, year"  in {
    val mentions = getMentions(sent15_2)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Sowing")
      m.arguments("value").head.text should equal("April 8")
      m.arguments("value").head.norms.get(0) should equal("XXXX-04-08")
    })
  }

  val sent15_3 = "Sowing on April 7, 2001 yields best outcomes."
  sent15_3 should "recognize sowing on [no verb attached] a Month followed by day, year"  in {
    val mentions = getMentions(sent15_3)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Sowing")
      m.arguments("value").head.text should equal("April 7, 2001")
      m.arguments("value").head.norms.get(0) should equal("2001-04-07")
    })
  }

  val sent15_4 = "The first sowing dates started on July 1st in 2010 and on July 8th in 2011"
  sent15_4 should "recognize range"  in {
    val mentions = getMentions(sent15_4)
    mentions.filter(_.label matches "Assignment") should have size (4)
    var count = 0
    for (m <- mentions.filter(_.label matches "Assignment")) {
      if (count == 0) {
        m.arguments("variable").head.text should be ("sowing dates")
        m.arguments("value").head.text should equal ("July 1st")
        m.arguments("value").head.norms.get(0) should equal("XXXX-07-01")
      } else if (count == 1){
        m.arguments("variable").head.text should be ("sowing dates")
        m.arguments("value").head.text should equal ("2010")
        m.arguments("value").head.norms.get(0) should equal("2010-XX-XX")
      }
      else if (count == 2) {
        m.arguments("variable").head.text should be ("sowing dates")
        m.arguments("value").head.text should equal ("July 8th")
        m.arguments("value").head.norms.get(0) should equal("XXXX-07-08")
      } else {
        m.arguments("variable").head.text should be ("sowing dates")
        m.arguments("value").head.text should equal ("2011")
        m.arguments("value").head.norms.get(0) should equal("2011-XX-XX")
      }
      count += 1
    }
  }

  val sent15_5 = "sowing ( July 15 )"
  sent15_5 should "recognize sowing on [no verb attached] date in parentheses"  in {
    val mentions = getMentions(sent15_5)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("sowing")
      m.arguments("value").head.text should equal("July 15")
      m.arguments("value").head.norms.get(0) should equal("XXXX-07-15")
    })
  }

  val sent16_5 = "Sowing (July 15 - August 15)"
  sent16_5 should "recognize sowing on [no verb attached] date range in parentheses"  in {
    val mentions = getMentions(sent16_5)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Sowing")
      m.arguments("value").head.text should equal("July 15 - August 15")
      m.arguments("value").head.norms.get(0) should equal("XXXX-07-15 -- XXXX-08-15")
    })
  }
  

  //TODO: these will pass after updating processors for date ranges
  
  // val sent15_6 = "Early sowing (before July 15)"
  // sent15_6 should "recognize sowing on [no verb attached] date range"  in {
  //   val mentions = getMentions(sent15_6)
  //   mentions.filter(_.label matches "Assignment") should have size (1)
  //   mentions.filter(_.label matches "Assignment").foreach({ m =>
  //     m.arguments("variable").head.text should be("sowing")
  //     m.arguments("value").head.text should equal("before July 15")
  //     m.arguments("value").head.norms.get(0) should equal("XXXX-01-01 -- XXXX-07-15")
  //   })
  // } 
  

  // val sent15_7 = "Late sowings (beyond August 15) total 82% achievements either 19,762 ha."
  // sent15_7 should "recognize sowing on [no verb attached] date range"  in {
  //   val mentions = getMentions(sent15_7)
  //   mentions.filter(_.label matches "Assignment") should have size (1)
  //   mentions.filter(_.label matches "Assignment").foreach({ m =>
  //     m.arguments("variable").head.text should be("sowings")
  //     m.arguments("value").head.text should equal("beyond August 15")
  //     m.arguments("value").head.norms.get(0) should equal("XXXX-08-15 -- XXXX-12-31")
  //   })
  // } 
  
  val sent16_7 = "sowing was done on 25th October"
  sent16_7 should "recognize sowing on [be verb in PP]"  in {
    val mentions = getMentions(sent16_7)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("sowing")
      m.arguments("value").head.text should equal("25th October")
      m.arguments("value").head.norms.get(0) should equal("XXXX-10-25")
    })
  }
  val sent16_8 = "sowing date began on July 8th"
  sent16_8 should "recognize sowing on [with verb]"  in {
    val mentions = getMentions(sent16_8)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("sowing date")
      m.arguments("value").head.text should equal("July 8th")
      m.arguments("value").head.norms.get(0) should equal("XXXX-07-08")
    })
  }
  //TODO: these will pass after updating processors for dates

  // val sent16_9 = "planting around July 15th led to the highest aerial biomass and grain yield for both years"
  // sent16_9 should "recognize sowing with the word [around]"  in {
  //   val mentions = getMentions(sent16_9)
  //   mentions.filter(_.label matches "Assignment") should have size (1)
  //   mentions.filter(_.label matches "Assignment").foreach({ m =>
  //     m.arguments("variable").head.text should be("sowing")
  //     m.arguments("value").head.text should equal("July 15th")
  //     m.arguments("value").head.norms.get(0) should equal("XXXX-07-15")
  //   })

  // val sent16_10 = "Sowing after July 15 th reduced aerial biomass and grain yield"
  // sent16_10 should "recognize sowing with the word [after]"  in {
  //   val mentions = getMentions(sent16_10)
  //   mentions.filter(_.label matches "Assignment") should have size (1)
  //   mentions.filter(_.label matches "Assignment").foreach({ m =>
  //     m.arguments("variable").head.text should be("Sowing")
  //     m.arguments("value").head.text should equal("after July 15")
  //     m.arguments("value").head.norms.get(0) should equal("XXXX-07-15 -- XXXX-12-31")
  //   })


  // Tests for CULTIVARS 

  val sent16 = "The most important planted cash crop is peanut in the SRV."
  sent16 should "recognize crop attached to be verb "in {
    val mentions = getMentions(sent16)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("crop")
      m.arguments("value").head.text should equal("peanut")
    })
  }

    val sent16_0 = "Most farmers in the SRV plant Sahel 108"
  sent16_0 should "recognize crops attached to be verb "in {
    val mentions = getMentions(sent16_0)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("plant")
      m.arguments("value").head.text should equal("Sahel 108")
    })
  }

  val sent16_1 = "Some farmers use variety like Sahel 108"
  sent16_1 should "recognize cultivar attached to preposition "in {
    val mentions = getMentions(sent16_1)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("variety")
      m.arguments("value").head.text should equal("Sahel 108")
    })
  }

  val sent16_2 = "The productivity of a range of agricultural crops beyond rice"
  sent16_2 should "recognise cultures preceded by a preposition"in {
    val mentions = getMentions(sent16_2)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("crops")
      m.arguments("value").head.text should equal("rice")
    })
  }

  val sent16_3 = "In the SRV where irrigated rice is the most common grown crop"
  sent16_3 should "recognize crop attached to be verb"in {
    val mentions = getMentions(sent16_3)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("crop")
      m.arguments("value").head.text should equal("rice") 
    })
  }

  val sent16_2_1 = "Farmers use cultivar such as sugarcane which are planted on 80 and 20 percent of total area"
  sent16_2_1 should "recognize crops  preceded by adj + prep "in {
    val mentions = getMentions(sent16_2_1)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("cultivar")
      m.arguments("value").head.text should equal("sugarcane")
    })
  }
  val sent16_2_2 = "AfricaRice, a CGIAR research center, developed the seed Sahel 108"
  sent16_2_2 should "recognize crop preceded by adj + prep "in {
    val mentions = getMentions(sent16_2_2)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("seed")
      m.arguments("value").head.text should equal("Sahel 108")
    })
  }

  val sent16_2_3 = "Farmers preferred to use short duration rice varieties like Sahel 108"
  sent16_2_3 should "recognize crop preceded by adj + prep "in {
    val mentions = getMentions(sent16_2_3)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("varieties")
      m.arguments("value").head.text should equal("Sahel 108")
    })
  }

  val sent16_2_4 = "Other crops cultivated include millet"
  sent16_2_4 should "recognize crops [attached to verb]" in {
    val mentions = getMentions(sent16_2_4)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("crops")
      m.arguments("value").head.text should equal("millet")
    })
  }

  val sent16_3_1 = "Other crops cultivated include millet, sorghum, maize, cowpea and vegetables"
  sent16_3_1 should "recognize comma separated crops"  in {
    val mentions = getMentions(sent16_3_1)
    mentions.filter(_.label matches "Assignment") should have size (5)
    var count = 0
    
    for (m <- mentions.filter(_.label matches "Assignment")) {
      if (count == 0) {
        m.arguments("variable").head.text should be ("crops")
        m.arguments("value").head.text should equal ("millet")
      } else if (count == 1) {
        m.arguments("variable").head.text should be ("crops")
        m.arguments("value").head.text should equal ("sorghum")
      } else if (count == 2){
        m.arguments("variable").head.text should be ("crops")
        m.arguments("value").head.text should equal ("maize")
      } else if (count == 3) {
        m.arguments("variable").head.text should be ("crops")
        m.arguments("value").head.text should equal ("cowpea")                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
      } else {
        m.arguments("variable").head.text should be ("crops")
        m.arguments("value").head.text should equal ("vegetables")
      }
      count += 1
    }
  }

  val sent16_4 = "In the 1998WS, farmers sowed Jaya between 20 June and 1 July"
  sent16_4 should "recognize crop attached to var"in {
    val mentions = getMentions(sent16_4)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("sowed")
      m.arguments("value").head.text should be("Jaya")

    })
  }

  val birdattack_1 = "Bird attacks occurred between July 1st and August 31st"
  birdattack_1 should "recognize bird attacks attached to var"in {
    val mentions = getMentions(birdattack_1)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Bird attacks")
      m.arguments("value").head.text should be("between July 1st and August 31st")

    })
  }

  val sent16_4_1 = "They chose furthermore to grow only one cultivar groundnut"
  sent16_4_1 should "recognize other variables for crop such as cultivar"in {
    val mentions = getMentions(sent16_4_1)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("cultivar")
      m.arguments("value").head.text should equal("groundnut")
    })
  }

  val sent16_4_2 = "In the SRV, farmers plant Sahel 108, Sahel 150, Sahel 154, Sahel 134, Nerica"
  sent16_4_2 should "recognize crop [attached to dates]"in {
    val mentions = getMentions(sent16_4_2)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      var count = 0
      m.arguments("value") should have size (5)
      
      for (v <- m.arguments("value")) {
        if (count == 0) {
          v.text should equal ("Sahel 108")
        }else if (count == 1) {
          v.text should equal ("Sahel 150")
        }else if (count == 2) {
          v.text should equal ("Sahel 154")
        }else if (count == 3) {
          v.text should equal ("Sahel 134")
        }else {
          v.text should equal ("Nerica")
        }
        count += 1
      }      
    })
  }

  val sent17_1 = "Peanut, sugarcane and cotton are important cash crops."
  sent17_1 should "recognize crops comma separated"  in {
    val mentions = getMentions(sent17_1)
    mentions.filter(_.label matches "Assignment") should have size (3)
    var count = 0
    
    for (m <- mentions.filter(_.label matches "Assignment")) {
      if (count == 0) {
        m.arguments("variable").head.text should be ("crops")
        m.arguments("value").head.text should equal ("Peanut")
      } else if (count == 1) {
        m.arguments("variable").head.text should be ("crops")
        m.arguments("value").head.text should equal ("sugarcane")
      } else {
        m.arguments("variable").head.text should be ("crops")
        m.arguments("value").head.text should equal ("cotton")
      }
      count += 1
    }
  }

  val sent17_2 = "Millet, rice, corn and sorghum are the primary food crops grown in Senegal."
  sent17_2 should "recognize comma separated crops"  in {
    val mentions = getMentions(sent17_2)
    mentions.filter(_.label matches "Assignment") should have size (4)
    var count = 0
    
    for (m <- mentions.filter(_.label matches "Assignment")) {
      if (count == 0) {
        m.arguments("variable").head.text should be ("crops")
        m.arguments("value").head.text should equal ("Millet")
      } else if (count == 1) {
        m.arguments("variable").head.text should be ("crops")
        m.arguments("value").head.text should equal ("rice")
      } else if (count == 2){
        m.arguments("variable").head.text should be ("crops")
        m.arguments("value").head.text should equal ("corn")
      } else {
        m.arguments("variable").head.text should be ("crops")
        m.arguments("value").head.text should equal ("sorghum")
      }
      count += 1
    }
  }

  val sent17_3 = "Tomato or onion were the two most labour-consuming crops"
  sent17_3 should "recognize OR linked crops"  in {
    val mentions = getMentions(sent17_3)
    mentions.filter(_.label matches "Assignment") should have size (2)
    var count = 0
    
    for (m <- mentions.filter(_.label matches "Assignment")) {
      if (count == 0) {
        m.arguments("variable").head.text should be ("crops")
        m.arguments("value").head.text should equal ("Tomato")
      } else {
        m.arguments("variable").head.text should be ("crops")
        m.arguments("value").head.text should equal ("onion")
      }
      count += 1
    }
  }

  val sent17_4 = "Onion and tomato were the most profitable crops."
  sent17_4 should "recognize AND separated crops"  in {
    val mentions = getMentions(sent17_4)
    mentions.filter(_.label matches "Assignment") should have size (2)
    var count = 0
    
    for (m <- mentions.filter(_.label matches "Assignment")) {
      if (count == 0) {
        m.arguments("variable").head.text should be ("crops")
        m.arguments("value").head.text should equal ("Onion")
      } else {
        m.arguments("variable").head.text should be ("crops")
        m.arguments("value").head.text should equal ("tomato")
      }
      count += 1
    }
  }

  // Tests for Fertilizer var-val reading

 val sent_20 = "One of the most important farming input is mineral fertilizer"
  sent_20 should "recognize fertilizer [attached to be verb]"in {
    val mentions = getMentions(sent_20)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("input")
      m.arguments("value").head.text should equal("mineral")
    })
  }

 val sent_20_1 = "Fertilizer nitrogen (N) has been applied at two or more levels"
  sent_20_1 should "recognize fertilizer [attached to var]"in {
    val mentions = getMentions(sent_20_1)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Fertilizer")
      m.arguments("value").head.text should equal("nitrogen")
    })
  }

 val sent_20_2 = "In fact, use of fertilizer P has declined steadily since 1995"
  sent_20_2 should "recognize fertilizer [attached to variable label]"in {
    val mentions = getMentions(sent_20_2)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("fertilizer")
      m.arguments("value").head.text should equal("P")
    })
  }

 val sent_20_3 = "The amount of fertilizer N required was averaged at 52 kg ha-1"
  sent_20_3 should "recognize fertilizer [attached to variable]"in {
    val mentions = getMentions(sent_20_3)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("fertilizer")
      m.arguments("value").head.text should equal("N")
    })
  }

 val sent_20_4 = "The most widely used solid inorganic fertilizers are urea, diammonium phosphate and potassium chloride"
  sent_20_4 should "recognize fertilizer [attached to be verb]"in {
    val mentions = getMentions(sent_20_4)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("fertilizers")
      m.arguments("value").head.text should equal("urea")
    })
  }

 val sent_20_5 = "The most widely used solid inorganic fertilizers are diammonium phosphate, urea and potassium chloride"
  sent_20_5 should "recognize fertilizer [attached to be verb]"in {
    val mentions = getMentions(sent_20_5)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      var count = 0
      m.arguments("value") should have size (3)
      
      for (v <- m.arguments("value")) {
        if (count == 0) {
          v.text should equal ("diammonium phosphate")
        }else if (count == 1) {
          v.text should equal ("urea")
        }else {
          v.text should equal ("potassium chloride")
        }
        count += 1
      }
    }) 
  }

 val sent_20_5_1 = "The main nutrient elements present in the fertilizers are nitrogen, phosphorus, and potassium"
  sent_20_5_1 should "recognize fertilizer [attached to be verb]"in {
    val mentions = getMentions(sent_20_5_1)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      var count = 0
      m.arguments("value") should have size (3)
      
      for (v <- m.arguments("value")) {
        if (count == 0) {
          v.text should equal ("nitrogen")
        }else if (count == 1) {
          v.text should equal ("phosphorus")
        }else {
          v.text should equal ("potassium")
        }
        count += 1
      }
    }) 
  }

 val sent_20_5_2 = "The nitrogenous chemical fertilizers are urea, calcium, ammonium nitrate, ammonium sulfate, basic calcium nitrate, calcium cyanamide"
  sent_20_5_2 should "recognize fertilizer [attached to be verb]"in {
    val mentions = getMentions(sent_20_5_2)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      var count = 0
      m.arguments("value") should have size (6)
      
      for (v <- m.arguments("value")) {
        if (count == 0) {
          v.text should equal ("urea")
        }else if (count == 1) {
          v.text should equal ("calcium")
        }else if (count == 2){
          v.text should equal ("ammonium nitrate")
        }else if (count == 3) {
          v.text should equal ("ammonium sulfate")
        }else if (count == 4) {
          v.text should equal ("basic calcium nitrate")
        }else {
          v.text should equal ("calcium cyanamide")
        }
        count += 1
      }
    }) 
  }


 val sent_20_6 = "Total fertilizer usage was ammonium poly-phosphate on average 152 kg ha-1 in the 1999 and 2000WS"
  sent_20_6 should "recognize fertilizer [attached to be verb]"in {
    val mentions = getMentions(sent_20_6)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("fertilizer usage")
      m.arguments("value").head.text should equal("ammonium poly-phosphate")
    })
  }


 val sent_20_7 = "However, the most unvalable fertilizer was diammonium-phosphate"
  sent_20_7 should "recognize fertilizer [attached to be verb]"in {
    val mentions = getMentions(sent_20_7)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("fertilizer")
      m.arguments("value").head.text should equal("diammonium-phosphate")
    })
  }

  val sent_20_8 = "Phosphorus, potassium and NPK are important inorganic fertilizers."
  sent_20_8 should "recognize comma separated fertilizers types"  in {
    val mentions = getMentions(sent_20_8)
    mentions.filter(_.label matches "Assignment") should have size (3)
    var count = 0
    
    for (m <- mentions.filter(_.label matches "Assignment")) {
      if (count == 0) {
        m.arguments("variable").head.text should be ("fertilizers")
        m.arguments("value").head.text should equal ("Phosphorus")
      } else if (count == 1) {
        m.arguments("variable").head.text should be ("fertilizers")
        m.arguments("value").head.text should equal ("potassium")
      } else {
        m.arguments("variable").head.text should be ("fertilizers")
        m.arguments("value").head.text should equal ("NPK")
      }
      count += 1
    }
  }

  val sent_20_9 = "Some organic fertilizers include nitrogen, phosphorus, and potassium as the three most important elements for plant nutrition"
  sent_20_9 should "recognize comma separated fertilizers types"  in {
    val mentions = getMentions(sent_20_9)
    mentions.filter(_.label matches "Assignment") should have size (3)
    var count = 0
    
    for (m <- mentions.filter(_.label matches "Assignment")) {
      if (count == 0) {
        m.arguments("variable").head.text should be ("fertilizers")
        m.arguments("value").head.text should equal ("nitrogen")
      } else if (count == 1) {
        m.arguments("variable").head.text should be ("fertilizers")
        m.arguments("value").head.text should equal ("phosphorus")
      } else {
        m.arguments("variable").head.text should be ("fertilizers")
        m.arguments("value").head.text should equal ("potassium")
      }
      count += 1
    }
  }

}
