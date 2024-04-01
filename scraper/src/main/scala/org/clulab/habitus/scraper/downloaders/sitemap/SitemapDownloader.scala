package org.clulab.habitus.scraper.downloaders.sitemap

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.SitemapDomain
import org.clulab.habitus.scraper.downloaders.GetPageDownloader
import org.clulab.habitus.scraper.scrapers.sitemap.RobotsScraper

class SitemapDownloader() extends GetPageDownloader(new SitemapDomain()) with SiteDownloader {
  val robotsScraper = new RobotsScraper()

  override def download(browser: Browser, page: Page, baseDirName: String, inquiryOpt: Option[String] = None): Boolean = {
    localDownload(page, baseDirName, cleaner)._2
  }
}
