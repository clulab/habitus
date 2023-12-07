package org.clulab.habitus.scraper.apps

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.browsers.HabitusBrowser
import org.clulab.habitus.scraper.corpora.PageCorpus
import org.clulab.habitus.scraper.downloaders.PageCorpusDownloader

/**
  * Take the list of URLs in corpusFileName and download the pages into the
  * directory structure under baseDirName.
  */
object IndexDownloaderApp extends App {
  val term = "pastoralist"
  val corpusFileName = args.lift(0).getOrElse(s"./scraper/corpora/uganda/$term/indexcorpus.txt")
  val baseDirName = args.lift(1).getOrElse(s"../corpora/uganda/$term/indexes")
  val corpus = PageCorpus(corpusFileName)
  val downloader = new PageCorpusDownloader(corpus)
  val browser: Browser = new HabitusBrowser()

  downloader.download(browser, baseDirName)
}
