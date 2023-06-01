package org.clulab.habitus.scraper.scrapers

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.Page
import org.clulab.utils.FileUtils

import java.io.File
import java.nio.file.Files
import scala.util.Using

abstract class Scraper(val domain: String) {

  def scrapeTo(browser: Browser, page: Page, baseDirName: String): Unit = {
    // get the filename for the page and basedir
    // read the text of the html file
    // scrape to string
    // save the result as .txt
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

    val path = page.url.getPath
    val fileName = clean(path)
    val locationName = s"$subDirName/$fileName.html"

    println(s"Downloading ${page.url.toString} to $locationName")

    val doc = browser.get(page.url.toString)
    val html = doc.toString

    Using.resource(FileUtils.printWriterFromFile(locationName)) { printWriter =>
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
