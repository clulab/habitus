package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.scrapes.IndexScrape

class AdomOnlineIndexScraper extends PageIndexScraper("www.adomonline.com") {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape = {
    val doc = browser.parseString(html)
    val links = (doc >> elementList("div.item-details > h3.entry-title > a"))
      .map(_.attr("href"))
    val correctedLinks = links.map { link =>
      link
          .replace("%c2%a2", "\u00A2")
          .replace("%e2%82%b5", "\u20B5")
          .replace("%ef%bb%bf", "\uFEFF")
    }
    val scrape = IndexScrape(correctedLinks)

    scrape
  }
}
