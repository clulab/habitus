package org.clulab.habitus.scraper.scrapers

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.Page
import org.clulab.utils.FileUtils

import java.io.File
import java.nio.file.Files
import scala.util.Using

abstract class Scraper(val domain: String) {

  def scrape(browser: Browser, html: String): String

  def scrapeTo(browser: Browser, page: Page, baseDirName: String): Unit = {
    val dirName = clean(domain)
    val subDirName = s"$baseDirName/$dirName"
    val path = clean(page.url.getPath)

    val htmlFileName = path + ".html"
    val htmlLocationName = s"$subDirName/$htmlFileName"
    val html = FileUtils.getTextFromFile(htmlLocationName)

    val txtFileName = path + ".txt"
    val txtLocationName = s"$subDirName/$txtFileName"
    val text = scrape(browser, html)

    Using.resource(FileUtils.printWriterFromFile(txtLocationName)) { printWriter =>
      printWriter.println(text)
    }
  }

  def matches(page: Page): Boolean = {
    val host = page.url.getHost

    // It is either the complete domain or a subdomain.
    host == domain || host.endsWith("." + domain)
  }

  def downloadTo(browser: Browser, page: Page, baseDirName: String): Unit = {
    val dirName = clean(domain)
    val subDirName = s"$baseDirName/$dirName"

    Files.createDirectories(new File(subDirName).toPath)

    val path = clean(page.url.getPath)
    val htmlFileName = path + ".html"
    val htmlLocationName = s"$subDirName/$htmlFileName"

    println(s"Downloading ${page.url.toString} to $htmlLocationName")

    val doc = browser.get(page.url.toString)
    val html = doc.toHtml

    Using.resource(FileUtils.printWriterFromFile(htmlLocationName)) { printWriter =>
      printWriter.println(html)
    }
  }

  def clean(name: String): String = {
    name.map { char =>
      if (char.isLetterOrDigit) char else '_'
    }
  }
}

object Scraper {
  val scrapers = Seq(
    new AdomOnlineScraper(),
    new CitiFmOnlineScraper(),
    new GhanaWebScraper()
  )

  def getScraper(page: Page): Scraper = {
    val scraperOpt = scrapers.find(_.matches(page))

    scraperOpt.get
  }
}
