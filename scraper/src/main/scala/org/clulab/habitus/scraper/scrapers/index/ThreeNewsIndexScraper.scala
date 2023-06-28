package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.ThreeNewsDomain
import org.clulab.habitus.scraper.scrapes.IndexScrape

class ThreeNewsIndexScraper extends PageIndexScraper(ThreeNewsDomain) {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape = {
    val doc = browser.parseString(html)
    val links = (doc >> elementList("div#tdi_15 div.td-module-meta-info h3 a"))
        .map(_.attr("href"))
        .map(decode)
    val scrape = IndexScrape(links)

    scrape
  }
}
