package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.apps.IndexScraperApp.{browser, corpus, scraper}
import org.clulab.habitus.scraper.{Corpus, Page}
import org.clulab.habitus.scraper.scrapers.Scraper
import org.clulab.habitus.scraper.scrapes.IndexScrape
import org.clulab.utils.FileUtils

import java.io.PrintWriter
import scala.util.Try

class PageIndexScraper(val domain: String) extends Scraper {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape

  def scrapeTo(browser: Browser, page: Page, printWriter: PrintWriter): Unit = {

  }

  def matches(page: Page): Boolean = {
    val host = page.url.getHost

    // It is either the complete domain or a subdomain.
    host == domain || host.endsWith("." + domain)
  }
}

class CorpusIndexScraper(val corpus: Corpus) {
  val scrapers: Seq[PageIndexScraper] = Seq(
  )

  def getPageScraper(page: Page): PageIndexScraper = {
    val scraperOpt = scrapers.find(_.matches(page))

    scraperOpt.get
  }

  def scrape(browser: Browser, baseDirName: String): Unit = {
    corpus.lines.foreach { line =>
      val page = Page(line)
      val scraper = getPageScraper(page)
      val scrapeTry = Try(scraper.scrapeTo(browser, page, baseDirName))
      val location = s"$baseDirName/$fileName"
      val html = FileUtils.getTextFromFile(location)

      scraper.scrape(browser, html)
    }
  }
}
