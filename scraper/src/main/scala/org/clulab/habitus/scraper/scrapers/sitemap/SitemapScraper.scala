package org.clulab.habitus.scraper.scrapers.sitemap

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.SitemapDomain
import org.clulab.habitus.scraper.scrapers.index.PageIndexScraper
import org.clulab.habitus.scraper.scrapes.IndexScrape

import java.io.PrintWriter
import scala.xml.Elem

class SitemapScraper extends PageIndexScraper(new SitemapDomain()) with SiteScraper {

  def getLinksFromElem(elem: Elem): Seq[String] = {
    val sites = (elem \\ "url" \\ "loc").map { elem =>
      elem.text
    }

    sites
  }

  def scrape(browser: Browser, page: Page, text: String): IndexScrape = ???

  override def scrapeTo(browser: Browser, page: Page, baseDirName: String, printWriter: PrintWriter): Unit = {
    val xml = readPage(page, baseDirName, cleaner)
    val elem = toXml(xml)
    val links = getLinksFromElem(elem)

    links.foreach { link =>
      printWriter.println(link)
    }
  }
}
