package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.GnaDomain
import org.clulab.habitus.scraper.scrapes.IndexScrape

class GnaIndexScraper extends PageIndexScraper(GnaDomain) {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape = {
    val doc = browser.parseString(html)
    val links = (doc >> elementList("header.entry-header h2.entry-title > a"))
        .map(_.attr("href"))
    val scrape = IndexScrape(links)

    scrape
  }
}
