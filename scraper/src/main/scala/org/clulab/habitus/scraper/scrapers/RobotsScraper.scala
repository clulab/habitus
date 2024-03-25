package org.clulab.habitus.scraper.scrapers

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.Page
import org.clulab.habitus.scraper.domains.RobotsDomain

import scala.io.Source

class RobotsScraper extends Scraper[Seq[String]](new RobotsDomain()) {
  val header = "Sitemap:"

  override def scrape(browser: Browser, page: Page, text: String): Seq[String] = {
    val localSitemapIndexes = Source
        .fromString(text).getLines().flatMap { line =>
          if (line.startsWith(header))
            Some(line.drop(header.length).trim)
          else
            None
        }
        .toVector
        .map { sitemapIndex =>
          sitemapIndex.replaceAll("/www.gna.org.gh/", "/gna.org.gh/")
        }
        .map { sitemapIndex =>
          sitemapIndex.replaceAll("/.xml", "/sitemap.xml")
        }

    localSitemapIndexes
  }
}
