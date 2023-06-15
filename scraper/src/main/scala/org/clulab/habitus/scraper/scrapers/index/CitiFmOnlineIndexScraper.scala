package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.scrapes.IndexScrape

class CitiFmOnlineIndexScraper extends PageIndexScraper("citifmonline.com") {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape = {
    val doc = browser.parseString(html)
    val links = (doc >> elementList("div.jeg_postblock_content > h3.jeg_post_title > a"))
        .map(_.attr("href"))
        .map(decode)
    val scrape = IndexScrape(links)

    scrape
  }
}
