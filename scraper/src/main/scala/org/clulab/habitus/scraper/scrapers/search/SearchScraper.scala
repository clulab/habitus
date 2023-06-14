package org.clulab.habitus.scraper.scrapers.search

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.{Cleaner, Corpus, Page}
import org.clulab.habitus.scraper.scrapers.Scraper
import org.clulab.habitus.scraper.scrapes.SearchScrape
import org.clulab.utils.FileUtils

import java.io.PrintWriter
import scala.util.{Try, Using}

abstract class PageSearchScraper(val domain: String) extends Scraper[SearchScrape] {
  val cleaner = new Cleaner()

  def scrape(browser: Browser, page: Page, html: String): SearchScrape

  def scrapeTo(browser: Browser, page: Page, baseDirName: String, printWriter: PrintWriter): Unit = {
    val dirName = cleaner.clean(domain)
    val subDirName = s"$baseDirName/$dirName"
    val file = cleaner.clean(page.url.getFile)

    val htmlFileName = file + ".html"
    val htmlLocationName = s"$subDirName/$htmlFileName"
    val html = FileUtils.getTextFromFile(htmlLocationName)

    val scraped = scrape(browser, page, html)

    // TODO Let the inquirer to this?
    1.to(scraped.count).foreach { index =>
      printWriter.println(s"${page.url.toString} - $index")
    }
  }

  def matches(page: Page): Boolean = {
    val host = page.url.getHost

    // It is either the complete domain or a subdomain.
    // TODO: Change this!
    host == domain || host.endsWith("." + domain)
  }
}

class CorpusSearchScraper(val corpus: Corpus) {
  val scrapers: Seq[PageSearchScraper] = Seq(
    new AdomOnlineSearchScraper(),
    new CitiFmOnlineSearchScraper(),
    new EtvGhanaSearchScraper(),
    new GhanaWebSearchScraper(),
    new GnaSearchScraper(),
    new HappyGhanaSearchScraper(),
    new TheChronicleSearchScraper(),
    new ThreeNewsSearchScraper()
  )

  def getPageScraper(page: Page): PageSearchScraper = {
    val scraperOpt = scrapers.find(_.matches(page))

    scraperOpt.get
  }

  def scrape(browser: Browser, baseDirName: String, fileName: String): Unit = {
    Using.resource(FileUtils.printWriterFromFile(fileName)) { printWriter =>
      corpus.lines.foreach { line =>
        val page = Page(line)
        val scraper = getPageScraper(page)
        val scrapeTry = Try(scraper.scrapeTo(browser, page, baseDirName, printWriter))

        if (scrapeTry.isFailure)
          println(s"Scrape of ${page.url.toString} failed!")
      }
    }
  }
}
