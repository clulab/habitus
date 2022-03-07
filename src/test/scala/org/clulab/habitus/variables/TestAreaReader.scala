package org.clulab.habitus.variables

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.clulab.odin.Mention

class TestAreaReader extends FlatSpec with Matchers {
  val vp = VariableProcessor("/variables/master-areas.yml")

  def getMentions(text: String): Seq[Mention] = {
    val (_, mentions, _, _) = vp.parse(text)
    mentions
  }

  behavior of "AreaReader"

  val sent1 = "28,223 ha vs 35,065 ha were used as sown areas"
  sent1 should "recognize area sizes" in {
    val areaMentions = getMentions(sent1).filter(_.label matches "Assignment")
    areaMentions should have size (1)
    areaMentions.foreach({ m =>
      m.arguments("variable").head.text should be("areas")
    })
    areaMentions.head.arguments("value").head.text should equal("28,223 ha")
//    areaMentions.last.arguments("value").head.text should equal("35,065 ha") //note: currently not extracting values after vs.

  }

  val sent2 = "The areas sown for this 2021/2022 wintering campaign are 28,223 ha vs 35,065 ha in wintering"
  sent2 should "recognize area sizes" in {
    val areaMentions = getMentions(sent2).filter(_.label matches "Assignment")
    areaMentions should have size (1)
    areaMentions.foreach({ m =>
      m.arguments("variable").head.text should be("areas")
    })
    areaMentions.head.arguments("value").head.text should equal("28,223 ha")
//    areaMentions.last.arguments("value").head.text should equal("35,065 ha") //note: currently not extracting values after vs.

  }

  val sent3 = "Harvests have started in some production areas of the valley, to date an area estimated at 843 ha is already harvested in  the Delta, 199 ha in Matam, 31 ha in Bakel, and 23 ha in Dagana."
  sent3 should "recognize area sizes" in {
    val areaMentions = getMentions(sent3).filter(_.label matches "Assignment")
    areaMentions should have size (3) // excluding "23 ha in Dagana" - with this, we are testing exclusion of excessive var-val distances
    areaMentions.foreach({ m =>
      m.arguments("variable").head.text should include ("area") // fixme: 843 still attaches to areas because "area estimated at 843 ha" has not been extracted
    })
    val values = areaMentions.map(_.arguments("value").head.text)
    values.length should equal(3)
    values should contain ("843 ha")
    values should contain ("199 ha")
    values should contain ("31 ha")

  }
}