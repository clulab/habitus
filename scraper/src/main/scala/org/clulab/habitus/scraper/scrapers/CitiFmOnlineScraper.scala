package org.clulab.habitus.scraper.scrapers

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList

class CitiFmOnlineScraper extends Scraper("citifmonline.com") {

  def scrape(browser: Browser, html: String): String = {
    val doc = browser.parseString(html)
    val title = doc.title
    val timestamp = (doc >> elementList("div.jeg_meta_date")).head.text.trim
    val paragraphs = doc >> elementList("div.content-inner > p")
    val source = paragraphs
        .sliding(2)
        .find { paragraphPair =>
          val prevParagraph = paragraphPair.head.text.trim
          val paragraph = paragraphPair.last.text.trim

          prevParagraph == "\u2013" && paragraph.startsWith(CitiFmOnlineScraper.sourcePrefix)
        }
        .map { paragraphPair =>
          paragraphPair.last.text.trim.substring(CitiFmOnlineScraper.sourcePrefix.length)
        }
        .getOrElse("[none]")
    val text = paragraphs
      .map { paragraph =>
        paragraph.text.trim
      }
      .filter(_.nonEmpty)
      .mkString("\n\n")

    s"$title\n\n$timestamp\n\n$source\n\n\n$text"
  }
}

object CitiFmOnlineScraper {
  val sourcePrefix = "Source: "
}