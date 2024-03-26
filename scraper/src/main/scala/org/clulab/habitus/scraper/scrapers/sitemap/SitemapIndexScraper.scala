package org.clulab.habitus.scraper.scrapers.sitemap

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.domains.SitemapIndexDomain
import org.clulab.habitus.scraper.inquirers.PageInquirer
import org.clulab.habitus.scraper.scrapers.search.PageSearchScraper
import org.clulab.habitus.scraper.scrapes.SearchScrape
import org.clulab.habitus.scraper.{Page, Search}
import org.clulab.utils.{FileUtils, Sourcer, StringUtils}

import java.io.{File, PrintWriter}
import scala.util.Using
import scala.xml.Elem

class SitemapIndexScraper extends PageSearchScraper(new SitemapIndexDomain()) with SiteScraper {
  val robotsScraper = new RobotsScraper()

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
    val sitemapIndexString = s"$home/sitemap_index.xml"
    val sitemapString = s"$home/sitemap.xml"

    val domain = page.url.getHost.split('.')/*.takeRight(2)*/.mkString(".") // Shorten to one .
    val dirName = cleaner.clean(domain)
    val subDirName = s"$baseDirName/$dirName"

    val file = cleaner.clean(page.url.getFile) // This may automatically take off the #comment.
    val localFileName = file // This is added even if it already ended in .html.
    val localLocationName = s"$subDirName/$localFileName"

    val robotsText = FileUtils.getTextFromFile(localLocationName)
    val sitemapIndexes = robotsScraper.scrape(browser, page, robotsText)
    val otherIndexes = Seq(sitemapIndexString, sitemapString).filter { siteString =>
      !sitemapIndexes.contains(siteString) && {
        val file = cleaner.clean(Page(siteString).url.getFile)
        val localFileName = file
        val localLocationName = s"$subDirName/$localFileName"

        new File(localLocationName).exists
      }
    }
    val distinctSitemapIndexes = (sitemapIndexes ++ otherIndexes).distinct
    val sitemaps = distinctSitemapIndexes.flatMap { sitemapIndex =>
      if (sitemapIndex.endsWith(".xml")) {

        val file = cleaner.clean(Page(sitemapIndex).url.getFile)
        val localFileName = file
        val localLocationName = s"$subDirName/$localFileName"
        val text = Using.resource(Sourcer.sourceFromFilename(localLocationName)) { sourcer =>
          sourcer.mkString
        }
        val elem = toXml(text)

        if (isSitemapIndex(elem))
          getSitemapsFromElem(elem)
        else
          Seq(sitemapIndex)
      }
      else Seq.empty
    }
    sitemaps.distinct.foreach { sitemap =>
      printWriter.println
    }
  }
}
