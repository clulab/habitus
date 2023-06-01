package org.clulab.habitus.scraper.scrapers

import net.ruippeixotog.scalascraper.browser.Browser

class GhanaWebScraper extends Scraper("ghanaweb.com") {

  def scrape(browser: Browser, html: String): String = {
    html
  }
}
