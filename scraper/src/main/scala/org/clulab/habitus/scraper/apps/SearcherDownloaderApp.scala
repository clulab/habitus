package org.clulab.habitus.scraper.apps

import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import org.clulab.habitus.scraper.corpora.SearchCorpus
import org.clulab.habitus.scraper.downloaders.SearchCorpusDownloader

/**
  * Take the list of URLs in corpusFileName and download the pages into the
  * directory structure under baseDirName.
  */
object SearcherDownloaderApp extends App {
  val corpusFileName = args.lift(0).getOrElse("./scraper/searchcorpus-illegal-mining.txt")
  val baseDirName = args.lift(1).getOrElse("../corpora/scraper-illegal-mining/searches")
  val searchCorpus = SearchCorpus(corpusFileName)
  val downloader = new SearchCorpusDownloader(searchCorpus)
  val browser: Browser = JsoupBrowser()

  downloader.download(browser, baseDirName)
}
