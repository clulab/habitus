package org.clulab.habitus.scraper.apps

import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import org.clulab.habitus.scraper.{Corpus, CorpusDownloader}

object IndexDownloaderApp extends App {
  val corpusFileName = args.lift(0).getOrElse("./scraper/indexcorpus.txt")
  val baseDirName = args.lift(1).getOrElse("../corpora/scraper/indexes")
  val corpus = Corpus(corpusFileName)
  val downloader = new CorpusDownloader(corpus)
  val browser: Browser = JsoupBrowser()

  downloader.download(browser, baseDirName)
}
