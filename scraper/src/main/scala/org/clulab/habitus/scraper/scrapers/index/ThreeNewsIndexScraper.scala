package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.scrapes.IndexScrape

class ThreeNewsIndexScraper extends PageIndexScraper("3news.com") {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape = {
    val doc = browser.parseString(html)
    val links = (doc >> elementList("div#tdi_15 div.td-module-meta-info h3 a"))
      .map(_.attr("href"))
    val correctedLinks = links.map { link =>
      link.replace("%c2%a2", "\u00A2")
    }
    val scrape = IndexScrape(correctedLinks)

    scrape
  }
}
