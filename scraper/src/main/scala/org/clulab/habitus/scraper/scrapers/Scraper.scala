package org.clulab.habitus.scraper.scrapers

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.domains.Domain
import org.clulab.habitus.scraper.{Cleaner, DomainSpecific, Page}
import org.clulab.utils.FileUtils

abstract class Scraper[T](domain: Domain) extends DomainSpecific(domain) {
  val cleaner = Scraper.cleaner

  def scrape(browser: Browser, page: Page, html: String): T

  def readHtml(page: Page, baseDirName: String): (String, String, String) = {
    val dirName = cleaner.clean(domain.domain)
    val subDirName = s"$baseDirName/$dirName"
    val file = cleaner.clean(page.url.getFile)

    val htmlFileName = file + ".html"
    val htmlLocationName = s"$subDirName/$htmlFileName"
    val html = FileUtils.getTextFromFile(htmlLocationName)

    (subDirName, file, html)
  }
}

object Scraper {
  val cleaner = new Cleaner()
}
