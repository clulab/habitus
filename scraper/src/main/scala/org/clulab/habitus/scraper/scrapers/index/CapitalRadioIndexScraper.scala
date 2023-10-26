package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.CapitalRadioDomain
import org.clulab.habitus.scraper.scrapes.IndexScrape

class CapitalRadioIndexScraper extends PageIndexScraper(CapitalRadioDomain) {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape = {
    val doc = browser.parseString(html)
    val links = (doc >> elementList("div.articles-result-item div.article-body a"))
        .map(_.attr("href"))
        .map(decode)
    val scrape = IndexScrape(links)

    scrape
  }
}
