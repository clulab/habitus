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
//  val corpusFileName = args.lift(0).getOrElse("./scraper/indexcorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/thechronicle_indexcorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/dailygraphic_indexcorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/threenews_indexcorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/etvghana_indexcorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/happyghana_indexcorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/gna_indexcorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/adomonline_indexcorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/citifmonline_indexcorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/indexcorpus-illegal-galamsey.txt")
  val corpusFileName = args.lift(0).getOrElse("./scraper/corpora/uganda/uganda farming/indexcorpus.txt")
  val baseDirName = args.lift(1).getOrElse("../corpora/uganda/uganda farming/indexes")
  val corpus = PageCorpus(corpusFileName)
  val downloader = new PageCorpusDownloader(corpus)
  val browser: Browser = new HabitusBrowser()

  downloader.download(browser, baseDirName)
}
