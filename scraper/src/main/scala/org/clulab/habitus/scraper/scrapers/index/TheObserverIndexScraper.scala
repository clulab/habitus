package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.TheObserverDomain
import org.clulab.habitus.scraper.scrapes.IndexScrape

class TheObserverIndexScraper extends PageIndexScraper(TheObserverDomain) {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape = {
    val doc = browser.parseString(html)
    val links = (doc >> elementList("dt.result-title > a"))
        .map(_.attr("href"))
        .map(decode)
        .map(link => s"https://${domain.domain}$link")
    val scrape = IndexScrape(links)

    scrape
  }
}
