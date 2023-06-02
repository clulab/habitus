package org.clulab.habitus.scraper.scrapers

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList

import scala.util.Try

class AdomOnlineScraper extends Scraper("adomonline.com") {

  def scrape(browser: Browser, html: String): String = {
    val doc = browser.parseString(html)
    val title = doc.title
    val timestamp = (doc >> elementList("time.entry-date")).head.text.trim
    val source = (doc >> elementList("div.td-post-source-via a")).headOption.map(_.text.trim).getOrElse("[none]")
    val paragraphs = doc >> elementList("div.td-post-content > p")
    val text = paragraphs
      .map { paragraph =>
        paragraph.text.trim
      }
      .filter(_.nonEmpty)
      .mkString("\n\n")

    s"$title\n\n$timestamp\n\n$source\n\n\n$text"
  }
}
