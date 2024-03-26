package org.clulab.habitus.scraper.downloaders.sitemap

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.{Page, RobotsParser, Site}
import org.clulab.habitus.scraper.domains.RobotsDomain
import org.clulab.habitus.scraper.downloaders.GetPageDownloader
import org.clulab.utils.StringUtils

import scala.util.Try

class RobotsDownloader() extends GetPageDownloader(new RobotsDomain()) with SiteDownloader {
  val robotsParser = new RobotsParser()

  override def download(browser: Browser, page: Page, baseDirName: String, inquiryOpt: Option[String] = None): Unit = {
    val urlString = page.url.toString
    val home = StringUtils.beforeLast(urlString, '/')
    val sitemapIndexString = s"$home/${Site.sitemapIndex}"
    val sitemapString = s"$home/${Site.sitemap}"

    val robotsPage = page
    val text = localDownload(robotsPage, baseDirName, cleaner)
    val sitemapIndexes = robotsParser.parse(text).distinct

    sitemapIndexes.map { sitemapIndex =>
      localDownload(Page(sitemapIndex), baseDirName, cleaner)
    }
    Seq(sitemapIndexString, sitemapString).foreach { siteString =>
      if (!sitemapIndexes.contains(siteString))
        Try(localDownload(Page(siteString), baseDirName, cleaner))
    }
  }
}
