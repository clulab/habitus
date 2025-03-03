package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.MercyCorpsDomain
import org.clulab.habitus.scraper.scrapes.IndexScrape

class MercyCorpsIndexScraper extends PageIndexScraper(MercyCorpsDomain) {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape = {
    val doc = browser.parseString(html)

    // Define the base URL
    val baseUrl = "https://www.mercycorps.org"

    // Extract article links based on Mercy Corps' HTML structure and append base URL
    val links = (doc >> elementList("h3.c-solr-search-result__title > a"))
      .map(_.attr("href"))
      .map(link => baseUrl + link) // Append base URL to relative paths
      .map(decode)

    val scrape = IndexScrape(links)
    scrape
  }
}
