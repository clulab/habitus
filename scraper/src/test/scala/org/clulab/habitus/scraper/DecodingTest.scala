package org.clulab.habitus.scraper

import java.nio.charset.StandardCharsets
import scala.util.matching.Regex

class DecodingTest extends Test {
  val pattern = "(%[0123456789ABCDEFabcdef]{2})+".r

  def decode(text: String): String = {
    val decoded = pattern.replaceAllIn(text, { regexMatch: Regex.Match =>
      val bytes = regexMatch
          .group(0)
          .split("%")
          .filter(_.nonEmpty)
          .map(Integer.parseInt(_, 16).toByte)
      val string = new String(bytes, StandardCharsets.UTF_8)

      string
    })

    decoded
  }

  behavior of "UTF-8 encoding in URLs"

  it should "decode properly" in {
    val tests: Seq[(String, String)] = Seq(
      (
        "https://www.adomonline.com/galamsey-cocobod-loses-%c2%a24-8-billion-in-western-region/",
        "https://www.adomonline.com/galamsey-cocobod-loses-¢4-8-billion-in-western-region/"
      ),
      (
        "https://www.adomonline.com/galamseyer-fined-%e2%82%b51-2-billion-for-illegal-mining/",
        "https://www.adomonline.com/galamseyer-fined-₵1-2-billion-for-illegal-mining/"
      ),
      (
        "https://www.adomonline.com/%ef%bb%bfabout29-billion-needed-to-reclaim-degradable-land-minister/",
        "https://www.adomonline.com/\ufeffabout29-billion-needed-to-reclaim-degradable-land-minister/"
      )
    )

    tests.foreach { case (input, expected) =>
      val actual = decode(input)

      actual should be (expected)
    }
  }
}
