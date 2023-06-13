package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.scrapers.Scraper
import org.clulab.habitus.scraper.{Cleaner, Corpus, Page}
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.clulab.utils.FileUtils

import scala.util.{Try, Using}

abstract class PageArticleScraper(val domain: String) extends Scraper[ArticleScrape] {
  val cleaner = new Cleaner()

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape

  def scrapeTo(browser: Browser, page: Page, baseDirName: String): Unit = {
    val dirName = cleaner.clean(domain)
    val subDirName = s"$baseDirName/$dirName"
    val file = cleaner.clean(page.url.getFile)

    val htmlFileName = file + ".html"
    val htmlLocationName = s"$subDirName/$htmlFileName"
    val html = FileUtils.getTextFromFile(htmlLocationName)

    val scraped = scrape(browser, page, html)

    val txtFileName = file + ".txt"
    val txtLocationName = s"$subDirName/$txtFileName"
    val text = scraped.toText

    val jsonFileName = file + ".json"
    val jsonLocationName = s"$subDirName/$jsonFileName"
    val json = scraped.toJson

    Using.resource(FileUtils.printWriterFromFile(txtLocationName)) { printWriter =>
      printWriter.println(text)
    }

    Using.resource(FileUtils.printWriterFromFile(jsonLocationName)) { printWriter =>
      printWriter.println(json)
    }
  }

  def matches(page: Page): Boolean = {
    val host = page.url.getHost

    // It is either the complete domain or a subdomain.
    host == domain || host.endsWith("." + domain)
  }
}

class CorpusArticleScraper(val corpus: Corpus) {
  val scrapers = Seq(
    new AdomOnlineArticleScraper(),
    new CitiFmOnlineArticleScraper(),
    new GhanaWebArticleScraper(),
    new TheChronicleArticleScraper(),
    new ThreeNewsArticleScraper(),
    new EtvGhanaArticleScraper(),
    new HappyGhanaArticleScraper(),
    new GnaArticleScraper()
  )

  def getPageScraper(page: Page): PageArticleScraper = {
    val scraperOpt = scrapers.find(_.matches(page))

    scraperOpt.get
  }

  def scrape(browser: Browser, baseDirName: String): Unit = {
    corpus.lines.foreach { line =>
      val page = Page(line)
      val scraper = getPageScraper(page)
      val scrapeTry = Try(scraper.scrapeTo(browser, page, baseDirName))

      if (scrapeTry.isFailure)
        println(s"Scrape of ${page.url.toString} failed!")
    }
  }
}
