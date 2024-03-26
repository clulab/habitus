package org.clulab.habitus.scraper.scrapers.search

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.corpora.SearchCorpus
import org.clulab.habitus.scraper.domains.Domain
import org.clulab.habitus.scraper.inquirers.{CorpusInquirer, PageInquirer}
import org.clulab.habitus.scraper.{Cleaner, Page, Search}
import org.clulab.habitus.scraper.scrapers.Scraper
import org.clulab.habitus.scraper.scrapers.sitemap.SitemapIndexScraper
import org.clulab.habitus.scraper.scrapes.SearchScrape
import org.clulab.utils.{FileUtils, ProgressBar}

import java.io.PrintWriter
import scala.util.{Try, Using}

abstract class PageSearchScraper(domain: Domain) extends Scraper[SearchScrape](domain) {

  def scrape(browser: Browser, page: Page, html: String): SearchScrape

  def scrapeTo(browser: Browser, page: Page, inquirer: PageInquirer, search: Search, baseDirName: String, printWriter: PrintWriter): Unit = {
    val page = inquirer.inquire(search.inquiry)
    val (_, _, html) = readHtml(page, baseDirName)
    val scraped = scrape(browser, page, html)

    1.to(scraped.count).foreach { index =>
      val page = inquirer.inquire(search.inquiry, Some(index))

      // TODO: This might be some other instruction if the is no GET access.
      printWriter.println(s"${page.url.toString}")
    }
  }
}

class CorpusSearchScraper(val corpus: SearchCorpus) {
  val scrapers: Seq[PageSearchScraper] = Seq(
    new AdomOnlineSearchScraper(),
    new CitiFmOnlineSearchScraper(),
    new EtvGhanaSearchScraper(),
    new GhanaWebSearchScraper(),
    new GnaSearchScraper(),
    new HappyGhanaSearchScraper(),
    new TheChronicleSearchScraper(),
    new ThreeNewsSearchScraper(),
    new GoogleSearchScraper()
  )

  def getPageScraper(page: Page): PageSearchScraper = {
    val scraperOpt = scrapers.find(_.matches(page))

    scraperOpt.get
  }

  def scrape(browser: Browser, baseDirName: String, fileName: String): Unit = {
    val corpusInquirer = new CorpusInquirer()

    Using.resource(FileUtils.printWriterFromFile(fileName)) { printWriter =>
      val progressBar = ProgressBar("CorpusSearchScraper.scrape", corpus.items)

      progressBar.foreach { search =>
        val page = search.page
        val inquiry = search.inquiry

        progressBar.setExtraMessage(page.url.toString)

        val scraper =
            if (inquiry == "robots") new SitemapIndexScraper()
            else getPageScraper(page)

        val inquirer = corpusInquirer.getPageInquirer(page)
        val scrapeTry = Try(scraper.scrapeTo(browser, page, inquirer, search, baseDirName, printWriter))

        if (scrapeTry.isFailure)
          println(s"Scrape of ${page.url.toString} failed!")
      }
    }
  }
}
