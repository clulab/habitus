package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.{ABCDomain, CeobsDomain}
import org.clulab.habitus.scraper.scrapes.IndexScrape

class ABCIndexScraper extends PageIndexScraper(ABCDomain) {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape = {
    val doc = browser.parseString(html)

    // Extract article links based on ABC News Australia's HTML structure
    val links = (doc >> elementList("div._content_52s80_22  > a._link_145wx_1 _srLinkHint_1hggg_41 _showVisited_145wx_27 _showFocus_145wx_60 _underlineNone_145wx_19"))
      .map(_.attr("href"))
      .map(decode)

    val scrape = IndexScrape(links)
    scrape
  }
}
