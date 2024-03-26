package org.clulab.habitus.scraper.scrapers.sitemap

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.domains.RobotsDomain
import org.clulab.habitus.scraper.inquirers.PageInquirer
import org.clulab.habitus.scraper.scrapers.search.PageSearchScraper
import org.clulab.habitus.scraper.scrapes.SearchScrape
import org.clulab.habitus.scraper.{Page, RobotsParser, Search, Site}
import org.clulab.utils.StringUtils

import java.io.{File, PrintWriter}
import scala.xml.Elem

class RobotsScraper extends PageSearchScraper(new RobotsDomain()) with SiteScraper {
  val robotsParser = new RobotsParser()

  override def scrape(browser: Browser, page: Page, html: String): SearchScrape = ???

  def getSitemapsFromElem(elem: Elem): Seq[String] = {
    val sitemaps = (elem \\ "sitemap" \\ "loc").map { elem =>
      elem.text
    }

    sitemaps
  }

  override def scrapeTo(browser: Browser, page: Page, inquirer: PageInquirer, search: Search, baseDirName: String, printWriter: PrintWriter): Unit = {
    val urlString = page.url.toString
    val home = StringUtils.beforeLast(urlString, '/')
    val sitemapIndexString = s"$home/${Site.sitemapIndex}"
    val sitemapString = s"$home/${Site.sitemap}"

    val domain = page.url.getHost.split('.')/*.takeRight(2)*/.mkString(".") // Shorten to one .
    val dirName = cleaner.clean(domain)
    val subDirName = s"$baseDirName/$dirName"

    val robotsText = readPage(page, baseDirName, cleaner)
    val sitemapIndexes = robotsParser.parse(robotsText)
    val otherIndexes = Seq(sitemapIndexString, sitemapString).filter { siteString =>
      !sitemapIndexes.contains(siteString) && {
        val file = cleaner.clean(Page(siteString).url.getFile)
        val localLocationName = s"$subDirName/$file"

        new File(localLocationName).exists
      }
    }
    val distinctSitemapIndexes = (sitemapIndexes ++ otherIndexes).distinct
    val sitemaps = distinctSitemapIndexes.flatMap { sitemapIndex =>
      if (sitemapIndex.endsWith(".xml")) {
        val xml = readPage(Page(sitemapIndex), baseDirName, cleaner)
        val elem = toXml(xml)

        if (isSitemapIndex(elem))
          getSitemapsFromElem(elem)
        else
          Seq(sitemapIndex)
      }
      else Seq.empty
    }

    sitemaps.distinct.foreach(printWriter.println)
  }
}
