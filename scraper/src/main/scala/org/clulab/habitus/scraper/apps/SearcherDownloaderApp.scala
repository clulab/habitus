package org.clulab.habitus.scraper.apps

import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import org.clulab.habitus.scraper.CorpusDownloader
import org.clulab.habitus.scraper.corpora.PageCorpus

/**
  * Take the list of URLs in corpusFileName and download the pages into the
  * directory structure under baseDirName.
  */
object SearcherDownloaderApp extends App {
  val corpusFileName = args.lift(0).getOrElse("./scraper/searchcorpus.txt")
  val baseDirName = args.lift(1).getOrElse("../corpora/scraper/searches")
  val corpus = PageCorpus(corpusFileName)
  val downloader = new CorpusDownloader(corpus)
  val browser: Browser = JsoupBrowser()

  downloader.download(browser, baseDirName)
}
