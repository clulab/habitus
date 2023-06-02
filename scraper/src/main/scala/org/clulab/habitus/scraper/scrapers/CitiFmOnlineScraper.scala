package org.clulab.habitus.scraper.scrapers

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList

case class Byline(text: String, prefix: String) {
  val source = text.substring(prefix.length)
}

object Byline {
  val prefixes = Seq(
    "Source: ",
    "By: "
  )

  def fromTextOption(text: String): Option[Byline] = {
    val prefixOpt = prefixes.find { sourcePrefix =>
      text.startsWith(sourcePrefix)
    }
    val bylineOpt = prefixOpt.map(Byline(text, _))

    bylineOpt
  }
}

class CitiFmOnlineScraper extends Scraper("citifmonline.com") {

  def scrape(browser: Browser, html: String): String = {
    val doc = browser.parseString(html)
    val title = doc.title
    val timestamp = (doc >> elementList("div.jeg_meta_date")).head.text.trim
    val paragraphs = (doc >> elementList("div.content-inner > p"))
        // Sometimes there are empty paragraphs after the byline so that it
        // isn't last.  Remove the empty ones preventatively.
        .filter { paragraph => paragraph.text.trim.nonEmpty }
    val bylineOpt = paragraphs.lastOption
        .flatMap { paragraph =>
          val text = paragraph.text.trim

          Byline.fromTextOption(text)
        }
    val source = bylineOpt
        .map(_.source)
        .getOrElse("[none]")
    val text = paragraphs
        .map { paragraph =>
          paragraph.text.trim
        }
        .filter { text =>
          text != "\u2013" &&
          !bylineOpt.exists(_.text == text)
        }
        .mkString("\n\n")

    s"$title\n\n$timestamp\n\n$source\n\n\n$text"
  }
}

object CitiFmOnlineScraper {
  val bylinePrefixes = Seq(
    "Source: ",
    "By: "
  )
}
