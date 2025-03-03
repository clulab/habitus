package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.CNNDomain
import org.clulab.habitus.scraper.scrapes.IndexScrape

class CNNIndexScraper extends PageIndexScraper(CNNDomain) {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape = {
    val doc = browser.parseString(html)

    // Extract article links from CNN's HTML structure
    val links = (doc >> elementList("a.container__link container__link--type-NewsArticle container_list-images-with-description__link"))
      .map(_.attr("href")) // Extract the article URL
      .map(decode)

    val scrape = IndexScrape(links)
    scrape
  }
}
