package org.clulab.habitus.scraper.downloaders

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.{DomainSpecific, Page}
import org.clulab.habitus.scraper.corpora.{PageCorpus, SearchCorpus}
import org.clulab.habitus.scraper.domains.Domain
import org.clulab.utils.ProgressBar

import scala.util.Try

abstract class PageDownloader(domain: Domain) extends DomainSpecific(domain){

  def download(browser: Browser, page: Page, baseDirName: String, inquiryOpt: Option[String] = None): Unit

  def isValidPage(page: Page): Boolean = true
}

class PageCorpusDownloader(val corpus: PageCorpus) {
  val downloaders = Seq(
    new AdomOnlineDownloader(),
    new CitiFmOnlineDownloader(),
    new EtvGhanaDownloader(),
    new GhanaWebDownloader(),
    new GnaDownloader(),
    new HappyGhanaDownloader(),
    new TheChronicleDownloader(),
    new ThreeNewsDownloader(),
    new TheIndependentDownloader(),
    new TheObserverDownloader(),
    new KfmDownloader(),
    new NbsDownloader(),
    new AnkoleTimesDownloader(),
    new NilePostDownloader(),
    new UgStandardDownloader(),
    new CapitalRadioDownloader(),
    new UbcDownloader(),
    new DeltaDownloader(),
    new GoogleDownloader(),
    new MiningReviewDownloader(),
    new MiningDownloader()
  )

  def getPageDownloader(page: Page): PageDownloader = {
    val downloaderOpt = downloaders.find(_.matches(page))

    downloaderOpt.get
  }

  def download(browser: Browser, baseDirName: String): Unit = {
    corpus.items.foreach { page =>
      val downloader = getPageDownloader(page)

      // Avoid this error to make real download errors all the more obvious.
      if (downloader.isValidPage(page)) {
        val downloadTry = Try(downloader.download(browser, page, baseDirName))

        if (downloadTry.isFailure)
          println(s"Download of ${page.url.toString} failed!")
      }
    }
  }
}

class SearchCorpusDownloader(val corpus: SearchCorpus) {
  val downloaders = Seq(
    new AdomOnlineDownloader(),
    new CitiFmOnlineDownloader(),
    new EtvGhanaDownloader(),
    new GhanaWebDownloader(),
    new GnaDownloader(),
    new HappyGhanaDownloader(),
    new TheChronicleDownloader(),
    new ThreeNewsDownloader(),
    new TheIndependentDownloader(),
    new TheObserverDownloader(),
    new KfmDownloader(),
    new NbsDownloader(),
    new AnkoleTimesDownloader(),
    new NilePostDownloader(),
    new UgStandardDownloader(),
    new CapitalRadioDownloader(),
    new UbcDownloader(),
    new DeltaDownloader(),
    new GoogleDownloader(),
    new MiningReviewDownloader(),
    new MiningDownloader()
  )

  def getPageDownloader(page: Page): PageDownloader = {
    val downloaderOpt = downloaders.find(_.matches(page))

    downloaderOpt.get
  }

  def download(browser: Browser, baseDirName: String): Unit = {
    val progressBar = ProgressBar("Downloader.download", corpus.items)

    progressBar.foreach { search =>
      val page = search.page
      progressBar.setExtraMessage(page.url.toString + " ")

      val downloader = getPageDownloader(page)

      // Avoid this error to make real download errors all the more obvious.
      if (downloader.isValidPage(page)) {
        val downloadTry = Try(downloader.download(browser, page, baseDirName, Some(search.inquiry)))

        if (downloadTry.isFailure)
          println(s"Download of ${page.url.toString} failed!")
      }
    }
  }
}
