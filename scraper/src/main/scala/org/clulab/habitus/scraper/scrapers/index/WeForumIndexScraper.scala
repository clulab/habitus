package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.WeForumDomain
import org.clulab.habitus.scraper.scrapes.IndexScrape

class WeForumIndexScraper extends PageIndexScraper(WeForumDomain) {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape = {
    val doc = browser.parseString(html)

    // Extract article links based on WEF's HTML structure
    val links = (doc >> elementList("div.gsc-webResult gsc-result > div.gsc-webResult gsc-result > div.gsc-thumbnail-inside > div.gs-title > a.gs-title"))
      .map(_.attr("href"))
      .map(decode)

    val scrape = IndexScrape(links)
    scrape
  }
}
