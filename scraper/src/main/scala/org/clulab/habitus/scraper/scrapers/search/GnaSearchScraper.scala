package org.clulab.habitus.scraper.scrapers.search

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.scrapes.SearchScrape

class GnaSearchScraper extends PageSearchScraper("gna.org.gh") {

  def scrape(browser: Browser, page: Page, html: String): SearchScrape = {
    val doc = browser.parseString(html)
    val links = (doc >> elementList("a.page-numbers"))
    val count = links(links.length - 2).text.toInt
    val scrape = SearchScrape(count)

    scrape
  }
}
