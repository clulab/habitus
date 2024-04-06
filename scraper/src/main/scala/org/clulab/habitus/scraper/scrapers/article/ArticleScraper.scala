package org.clulab.habitus.scraper.scrapers.article

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.corpora.PageCorpus
import org.clulab.habitus.scraper.domains.Domain
import org.clulab.habitus.scraper.scrapers.Scraper
import org.clulab.habitus.scraper.{Cleaner, Page}
import org.clulab.habitus.scraper.scrapes.ArticleScrape
import org.clulab.utils.{FileUtils, ProgressBar}

import scala.util.{Try, Using}

abstract class PageArticleScraper(domain: Domain) extends Scraper[ArticleScrape](domain) {

  def scrape(browser: Browser, page: Page, html: String): ArticleScrape

  def scrapeTo(browser: Browser, page: Page, baseDirName: String): Unit = {
    val (subDirName, file, html) = readHtml(page, baseDirName)
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
}

class CorpusArticleScraper(val corpus: PageCorpus) {
  val scrapers = Seq(
    // new ExperimentArticleScraper(),
    new SaedArticleScraper(),
    new AdomOnlineArticleScraper(),
    new CitiFmOnlineArticleScraper(),
    new EtvGhanaArticleScraper(),
    new GhanaWebArticleScraper(),
    new GnaArticleScraper(),
    new HappyGhanaArticleScraper(),
    new TheChronicleArticleScraper(),
    new ThreeNewsArticleScraper(),
    new TheIndependentArticleScraper(),
    new TheObserverArticleScraper(),
    new KfmArticleScraper(),
    new NbsArticleScraper(),
    new AnkoleTimesArticleScraper(),
    new UgStandardArticleScraper(),
    new CapitalRadioArticleScraper(),
    new DeltaArticleScraper(),
    new GoogleArticleScraper(),
    new MiningReviewArticleScraper(),
    new MiningArticleScraper(),
    new PdfFileArticleScraper(),
    new MailFileArticleScraper(),
    new InterviewFileArticleScraper()
  )

  def getPageScraper(page: Page): PageArticleScraper = {
    val scraperOpt = scrapers.find(_.matches(page))

    scraperOpt.get
  }

  def scrape(browser: Browser, baseDirName: String): Unit = {
    val progressBar = ProgressBar("CorpusArticleScraper.scrape", corpus.items)

    corpus.items.foreach { page =>
//      progressBar.setExtraMessage(page.url.toString)

      val scraper = getPageScraper(page)
      val scrapeTry = Try(scraper.scrapeTo(browser, page, baseDirName))

      if (scrapeTry.isFailure)
        println(s"Scrape of ${page.url.toString} failed!")
    }
  }
}
