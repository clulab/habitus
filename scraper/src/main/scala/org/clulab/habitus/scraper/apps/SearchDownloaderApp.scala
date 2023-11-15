package org.clulab.habitus.scraper.apps

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.browsers.HabitusBrowser
import org.clulab.habitus.scraper.corpora.SearchCorpus
import org.clulab.habitus.scraper.downloaders.SearchCorpusDownloader

/**
  * Take the list of URLs in corpusFileName and download the pages into the
  * directory structure under baseDirName.
  */
object SearchDownloaderApp extends App {
  val corpusFileName = args.lift(0).getOrElse("./scraper/corpora/uganda/transhumance/searchcorpus.txt")
  val baseDirName = args.lift(1).getOrElse("../corpora/uganda/transhumance/searches")
  val searchCorpus = SearchCorpus(corpusFileName)
  val downloader = new SearchCorpusDownloader(searchCorpus)
  val browser: Browser = new HabitusBrowser()

  downloader.download(browser, baseDirName)
}
