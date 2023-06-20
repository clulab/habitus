package org.clulab.habitus.scraper.downloaders

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.{DomainSpecific, Page}
import org.clulab.habitus.scraper.corpora.PageCorpus
import org.clulab.habitus.scraper.domains.Domain

import scala.util.Try

abstract class PageDownloader(domain: Domain) extends DomainSpecific(domain){

  def download(browser: Browser, page: Page, baseDirName: String): Unit

  def isValidPage(page: Page): Boolean = true
}

class CorpusDownloader(val corpus: PageCorpus) {
  val downloaders = Seq(
    new AdomOnlineDownloader(),
    new CitiFmOnlineDownloader(),
    new EtvGhanaDownloader(),
    new GhanaWebDownloader(),
    new GnaDownloader(),
    new HappyGhanaDownloader(),
    new TheChronicleDownloader(),
    new ThreeNewsDownloader()
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
