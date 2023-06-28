package org.clulab.habitus.scraper.scrapers.search

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.EtvGhanaDomain
import org.clulab.habitus.scraper.scrapes.SearchScrape
import org.clulab.utils.StringUtils

class EtvGhanaSearchScraper extends PageSearchScraper(EtvGhanaDomain) {

  def scrape(browser: Browser, page: Page, html: String): SearchScrape = {
    val doc = browser.parseString(html)
    val span = (doc >> elementList("span.page_info")).head
    val count = StringUtils.afterLast(span.text, ' ', true).toInt
    val scrape = SearchScrape(count)

    scrape
  }
}
