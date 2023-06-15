package org.clulab.habitus.scraper.downloaders

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.corpora.PageCorpus

import scala.util.Try

class CorpusDownloader(val corpus: PageCorpus) {
  val pageDownloader = new GetPageDownloader()

  def download(browser: Browser, baseDirName: String): Unit = {
    // TODO: This exception needs to be somewhere else.
    val validItems = corpus.items.filterNot { page =>
      val urlName = page.url.toString

      urlName.contains("--")
    }

    validItems.foreach { page =>
      val downloadTry = Try(pageDownloader.download(browser, page, baseDirName))

      if (downloadTry.isFailure)
        println(s"Download of ${page.url.toString} failed!")
    }
  }
}
