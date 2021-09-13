package org.clulab.variables

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
    val (_, mentions) = vp.parse(text)
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

  //TODO: ms of year do not captured by our numbers.
  val sent4 = "Sowing between October 4 and October 14 of 2020 was optimal."
  sent4 should "recognize sowing date with range Month followed by 4 of year"  in {
    val mentions = getMentions(sent4)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Sowing")
      m.arguments("value").head.text should equal("between October 4 and October 14 of 2020")
      m.arguments("value").head.norms.get(0) should equal("2020-10-04 -- 2020-10-14")
    })
  }

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

  //TODO: ms of year do not captured by our numbers.
  val sent6 = "Planting date was October 1 of 2021."
  sent6 should "recognize sowing date with a date consisting a month followed by day then [of a year]"  in {
    val mentions = getMentions(sent6)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Planting date")
      m.arguments("value").head.text should equal("October 1 of 2021")
      m.arguments("value").head.norms.get(0) should equal("2021-10-01")
    })
  }

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
  //TODO: ms, see the last 3 tests, this is something very bizzare with unlucky date number 8 follow by [,year]
  val sent11 = "Sowing on October 8, 2019."
  sent11 should "recognize sowing on [no verb attached] a Month followed by day, year"  in {
    val mentions = getMentions(sent11)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Sowing")
      m.arguments("value").head.text should equal("October 8, 2019")
      m.arguments("value").head.norms.get(0) should equal("2019-10-08")
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

  //TODO: ms of year do not captured by our numbers.
  val sent12 = "Sowing between October 4 and October 14 of 2020 was optimal."
  sent12 should "recognize sowing range on a Month followed by day of year"  in {
    val mentions = getMentions(sent12)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Sowing")
      m.arguments("value").head.text should equal("between October 4 and October 14 of 2020")
      m.arguments("value").head.norms.get(0) should equal("2020-10-04 -- 2020-10-14")
    })
  }

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

  //TODO: ms, this is very bizzare behavior, just with date of 8. When it is followed by [, year] it fails.
  val sent15 = "Sowing on April 8, 2001 yields best outcomes."
  sent15 should "recognize sowing on [no verb attached] a Month followed by day, year"  in {
    val mentions = getMentions(sent15)
    mentions.filter(_.label matches "Assignment") should have size (1)
    mentions.filter(_.label matches "Assignment").foreach({ m =>
      m.arguments("variable").head.text should be("Sowing")
      m.arguments("value").head.text should equal("April 8, 2001")
      m.arguments("value").head.norms.get(0) should equal("2001-04-08")
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

  //TODO: ms, but with different date number it does not complain.
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

}
