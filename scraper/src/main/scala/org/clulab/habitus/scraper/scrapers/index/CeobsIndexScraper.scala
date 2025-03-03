package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.CeobsDomain
import org.clulab.habitus.scraper.scrapes.IndexScrape

class CeobsIndexScraper extends PageIndexScraper(CeobsDomain) {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape = {
    val doc = browser.parseString(html)

    // Extract article links based on the website's HTML structure
    val links = (doc >> elementList("h3.entry-title > a"))
      .map(_.attr("href"))
      .map(decode)

    val scrape = IndexScrape(links)
    scrape
  }
}
