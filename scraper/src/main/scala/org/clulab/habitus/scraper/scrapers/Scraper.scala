package org.clulab.habitus.scraper.scrapers

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.Page

trait Scraper[T] {
  def scrape(browser: Browser, page: Page, html: String): T
}
