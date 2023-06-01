package org.clulab.habitus.scraper.scrapers

import net.ruippeixotog.scalascraper.browser.Browser

class CitiFmOnlineScraper extends Scraper("citifmonline.com") {

  def scrape(browser: Browser, html: String): String = {
    html
  }
}
