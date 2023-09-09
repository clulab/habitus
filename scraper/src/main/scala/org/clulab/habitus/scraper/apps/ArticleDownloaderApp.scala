package org.clulab.habitus.scraper.apps

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.browsers.HabitusBrowser
import org.clulab.habitus.scraper.corpora.PageCorpus
import org.clulab.habitus.scraper.downloaders.PageCorpusDownloader

object ArticleDownloaderApp extends App {
//  val corpusFileName = args.lift(0).getOrElse("./scraper/articlecorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/thechronicle_articlecorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/threenews_articlecorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/etvghana_articlecorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/happyghana_articlecorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/gna_articlecorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/adomonline_articlecorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/citifmonline_articlecorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/articlecorpus-illegal-galamsey.txt")
  val corpusFileName = args.lift(0).getOrElse("./scraper/corpora/multi2/livestock/articlecorpus.txt")
  val baseDirName = args.lift(1).getOrElse("../corpora/multi2/livestock/articles")
  val corpus = PageCorpus(corpusFileName)
  val downloader = new PageCorpusDownloader(corpus)
  val browser: Browser = new HabitusBrowser()

  downloader.download(browser, baseDirName)
}
