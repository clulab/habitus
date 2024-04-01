package org.clulab.habitus.scraper.scrapers.index

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.corpora.PageCorpus
import org.clulab.habitus.scraper.domains.Domain
import org.clulab.habitus.scraper.{Cleaner, Page}
import org.clulab.habitus.scraper.scrapers.Scraper
import org.clulab.habitus.scraper.scrapers.sitemap.SitemapScraper
import org.clulab.habitus.scraper.scrapes.IndexScrape
import org.clulab.utils.{FileUtils, ProgressBar}

import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import scala.util.matching.Regex
import scala.util.{Try, Using}

abstract class PageIndexScraper(domain: Domain) extends Scraper[IndexScrape](domain) {

  def scrape(browser: Browser, page: Page, html: String): IndexScrape

  def decode(text: String): String = {
    val decoded = PageIndexScraper.utf8Pattern.replaceAllIn(text, { regexMatch: Regex.Match =>
      val bytes = regexMatch
          .group(0)
          .split("%")
          .filter(_.nonEmpty)
          .map(Integer.parseInt(_, 16).toByte)
      val string = new String(bytes, StandardCharsets.UTF_8)

      string
    })

    decoded
  }

  def scrapeTo(browser: Browser, page: Page, baseDirName: String, printWriter: PrintWriter): Unit = {
    val (_, _, html) = readHtml(page, baseDirName)
    val scraped = scrape(browser, page, html)

    scraped.links.foreach { link =>
      printWriter.println(link)
    }
  }
}

object PageIndexScraper {
  val utf8Pattern = "(%[0123456789ABCDEFabcdef]{2})+".r
}

class CorpusIndexScraper(val corpus: PageCorpus) {
  val scrapers: Seq[PageIndexScraper] = Seq(
    new SitemapScraper(),
    new AdomOnlineIndexScraper(),
    new CitiFmOnlineIndexScraper(),
    new EtvGhanaIndexScraper(),
    new GhanaWebIndexScraper(),
    new GnaIndexScraper(),
    new HappyGhanaIndexScraper(),
    new TheChronicleIndexScraper(),
    new ThreeNewsIndexScraper(),
    new TheIndependentIndexScraper(),
    new TheObserverIndexScraper(),
    new KfmIndexScraper(),
    new NbsIndexScraper(),
    new AnkoleTimesIndexScraper(),
    new UgStandardIndexScraper(),
    new CapitalRadioIndexScraper(),
    new DeltaIndexScraper(),
    new GoogleIndexScraper(),
    new MiningReviewIndexScraper(),
    new MiningIndexScraper()
  )

  def getPageScraper(page: Page): PageIndexScraper = {
    val scraperOpt = scrapers.find(_.matches(page))

    scraperOpt.get
  }

  def scrape(browser: Browser, baseDirName: String, fileName: String): Unit = {
    Using.resource(FileUtils.printWriterFromFile(fileName)) { printWriter =>
      val progressBar = ProgressBar("CorpusIndexScraper.scrape", corpus.items)

      progressBar.foreach { page =>
        // progressBar.setExtraMessage(page.url.toString)

        val scraper = getPageScraper(page)
        val scrapeTry = Try(
          scraper.scrapeTo(browser, page, baseDirName, printWriter)
        )

        if (scrapeTry.isFailure)
          println(s"Scrape of ${page.url.toString} failed!")
      }
    }
  }
}
