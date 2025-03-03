package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.GulfTimesDomain
import org.clulab.habitus.scraper.scrapes.IndexScrape

class GulfTimesIndexScraper extends PageIndexScraper(GulfTimesDomain) {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape = {
    val doc = browser.parseString(html)

    // Extract article links based on Gulf Times' HTML structure
    val links = (doc >> elementList("h2.article-title > a"))
      .map(_.attr("href"))
      .map(decode)

    val scrape = IndexScrape(links)
    scrape
  }
}
