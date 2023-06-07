package org.clulab.habitus.scraper.scrapers

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.{Page, Scrape}

class AdomOnlineScraper extends Scraper("adomonline.com") {

  def scrape(browser: Browser, page: Page, html: String): Scrape = {
    val doc = browser.parseString(html)
    val title = doc.title
    val timestamp = (doc >> elementList("time.entry-date")).head.text.trim
    val sourceOpt = (doc >> elementList("div.td-post-source-via a")).headOption.map(_.text.trim)
    val paragraphs = doc >> elementList("div.td-post-content > p")
    val text = paragraphs
      .map { paragraph =>
        paragraph.text.trim
      }
      .filter(_.nonEmpty)
      .mkString("\n\n")

    Scrape(page.url, Some(title), Some(timestamp), sourceOpt, text)
  }
}
