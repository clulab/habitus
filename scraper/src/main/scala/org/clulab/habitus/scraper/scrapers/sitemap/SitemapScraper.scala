package org.clulab.habitus.scraper.scrapers.sitemap

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.{Page, Search}
import org.clulab.habitus.scraper.domains.SitemapDomain
import org.clulab.habitus.scraper.scrapers.index.PageIndexScraper
import org.clulab.habitus.scraper.scrapes.IndexScrape
import org.clulab.utils.FileUtils

import java.io.PrintWriter
import scala.xml.{Elem, XML}

class SitemapScraper extends PageIndexScraper(new SitemapDomain()) with SiteScraper {

  def getLinksFromElem(elem: Elem): Seq[String] = {
    val sites = (elem \\ "url" \\ "loc").map { elem =>
      elem.text
    }

    sites
  }

  def readXml(page: Page, baseDirName: String): (String, String, String) = {
    val domain = page.url.getHost.split('.')/*.takeRight(2)*/.mkString(".") // Shorten to one .
    val dirName = cleaner.clean(domain)
    val subDirName = s"$baseDirName/$dirName"
    val file = cleaner.clean(page.url.getFile)

    val xmlFileName = file
    val xmlLocationName = s"$subDirName/$xmlFileName"
    val xml = FileUtils.getTextFromFile(xmlLocationName)

    (subDirName, file, xml)
  }

  def scrape(browser: Browser, page: Page, text: String): IndexScrape = ???

  override def scrapeTo(browser: Browser, page: Page, baseDirName: String, printWriter: PrintWriter): Unit = {
    val (_, _, xml) = readXml(page, baseDirName)
    val elem = toXml(xml)
    val links = getLinksFromElem(elem)

    links.foreach { link =>
      printWriter.println(link)
    }
  }
}
