package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.DeeplyHumanitarianDomain
import org.clulab.habitus.scraper.scrapes.IndexScrape

class DeeplyHumanitarianIndexScraper extends PageIndexScraper(DeeplyHumanitarianDomain) {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape = {
    val doc = browser.parseString(html)

    // Extract article links based on Syria News' HTML structure
    val links = (doc >> elementList("article.article > a[itemprop='url']"))
      .map(_.attr("href"))
      .map(decode)

    val scrape = IndexScrape(links)
    scrape
  }
}
