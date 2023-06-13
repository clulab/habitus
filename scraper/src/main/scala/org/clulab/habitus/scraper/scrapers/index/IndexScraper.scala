package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.{Cleaner, Corpus, Page}
import org.clulab.habitus.scraper.scrapers.Scraper
import org.clulab.habitus.scraper.scrapes.IndexScrape
import org.clulab.utils.FileUtils

import java.io.PrintWriter
import scala.util.{Try, Using}

abstract class PageIndexScraper(val domain: String) extends Scraper[IndexScrape] {
  val cleaner = new Cleaner()

  def scrape(browser: Browser, page: Page, html: String): IndexScrape

  def scrapeTo(browser: Browser, page: Page, baseDirName: String, printWriter: PrintWriter): Unit = {
    val dirName = cleaner.clean(domain)
    val subDirName = s"$baseDirName/$dirName"
    val file = cleaner.clean(page.url.getFile)

    val htmlFileName = file + ".html"
    val htmlLocationName = s"$subDirName/$htmlFileName"
    val html = FileUtils.getTextFromFile(htmlLocationName)

    val scraped = scrape(browser, page, html)

    scraped.links.foreach { link =>
      printWriter.println(link)
    }
  }

  def matches(page: Page): Boolean = {
    val host = page.url.getHost

    // It is either the complete domain or a subdomain.
    host == domain || host.endsWith("." + domain)
  }
}

class CorpusIndexScraper(val corpus: Corpus) {
  val scrapers: Seq[PageIndexScraper] = Seq(
    new GhanaWebIndexScraper(),
    new TheChronicleIndexScraper(),
    new ThreeNewsIndexScraper()
  )

  def getPageScraper(page: Page): PageIndexScraper = {
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
