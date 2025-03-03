package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.WfpusaDomain
import org.clulab.habitus.scraper.scrapes.IndexScrape

class WfpusaIndexScraper extends PageIndexScraper(WfpusaDomain) {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape = {
    val doc = browser.parseString(html)

    // Extract article links based on WFP USA's HTML structure
    val links = (doc >> elementList("div.gs-title > a"))
      .map(_.attr("href"))
      .map(decode)
      .filter(link => !link.endsWith(".pdf")) // Filter out PDF links

    val scrape = IndexScrape(links)
    scrape
  }
}
