package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.SyrianObserverDomain
import org.clulab.habitus.scraper.scrapes.IndexScrape

class SyrianObserverIndexScraper extends PageIndexScraper(SyrianObserverDomain) {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape = {
    val doc = browser.parseString(html)
    val links = (doc >> elementList("div.elementor-post__text > h3.elementor-post__title > a"))
      .map(_.attr("href"))
      .map(decode)
    val scrape = IndexScrape(links)

    scrape
  }
}
