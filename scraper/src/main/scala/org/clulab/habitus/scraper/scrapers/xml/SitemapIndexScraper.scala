package org.clulab.habitus.scraper.scrapers.xml

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.{Page, Search}
import org.clulab.habitus.scraper.domains.SitemapIndexDomain
import org.clulab.habitus.scraper.inquirers.PageInquirer
import org.clulab.habitus.scraper.scrapers.RobotsScraper
import org.clulab.habitus.scraper.scrapes.SearchScrape
import org.clulab.utils.Sourcer

import java.io.{File, PrintWriter}
import java.net.URL
import scala.util.Using
import scala.xml.Elem

class SitemapIndexScraper extends SiteScraper(new SitemapIndexDomain()) {
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
    val robotsString = urlString + "/robots.txt"
    val sitemapIndexString = urlString + "/sitemap_index.xml"
    val sitemapString = urlString + "/sitemap.xml"

    val domain = page.url.getHost.split('.')/*.takeRight(2)*/.mkString(".") // Shorten to one .
    val dirName = cleaner.clean(domain)
    val subDirName = s"$baseDirName/$dirName"

    val robotsPage = new Page(new URL(robotsString))

    val file = cleaner.clean(robotsPage.url.getFile) // This may automatically take off the #comment.
    val localFileName = file // This is added even if it already ended in .html.
    val localLocationName = s"$subDirName/$localFileName"

    val robotsText = Using.resource(Sourcer.sourceFromFilename(localLocationName)) { sourcer =>
      sourcer.mkString
    }
    val sitemapIndexes = robotsScraper.scrape(browser, robotsPage, robotsText)
    val otherIndexes = Seq(sitemapIndexString, sitemapString).filter { siteString =>
      !sitemapIndexes.contains(siteString) && {
        val file = cleaner.clean(Page(siteString).url.getFile)
        val localFileName = file
        val localLocationName = s"$subDirName/$localFileName"

        new File(localLocationName).exists
      }
    }
    val distinctSitemapIndexes = (sitemapIndexes ++ otherIndexes).distinct

    distinctSitemapIndexes.foreach { sitemapIndex =>
      if (sitemapIndex.endsWith(".xml")) {

        val file = cleaner.clean(Page(sitemapIndex).url.getFile)
        val localFileName = file
        val localLocationName = s"$subDirName/$localFileName"
        val text = Using.resource(Sourcer.sourceFromFilename(localLocationName)) { sourcer =>
          sourcer.mkString
        }
        val elem = toXml(text)

        if (isSitemapIndex(elem)) {
          val sitemaps = getSitemapsFromElem(elem)

          sitemaps.foreach { sitemap =>
            printWriter.println(sitemap)
          }
        }
        else {
          val sitemap = sitemapIndex

          assert(isSitemap(elem))
          printWriter.println(sitemap)
        }
      }
    }
  }
}
