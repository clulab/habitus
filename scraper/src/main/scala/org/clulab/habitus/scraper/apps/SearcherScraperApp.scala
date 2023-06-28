package org.clulab.habitus.scraper.apps

import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import org.clulab.habitus.scraper.corpora.SearchCorpus
import org.clulab.habitus.scraper.scrapers.search.CorpusSearchScraper

/**
  * Take the list of URLs in corpusFileName, find the downloaded pages, scrape them,
  * and put the links found into articleFileName so that they can be downloaded and
  * later scraped themselves.
  */

object SearcherScraperApp extends App {
  val corpusFileName = args.lift(0).getOrElse("./scraper/searchcorpus-illegal-mining.txt")
  val articleFileName = args.lift(1).getOrElse("./scraper/indexcorpus-illegal-mining.txt")
  val baseDirName = args.lift(1).getOrElse("../corpora/scraper-illegal-mining/searches")
  val corpus = SearchCorpus(corpusFileName)
  val scraper = new CorpusSearchScraper(corpus)
  val browser: Browser = JsoupBrowser()

  scraper.scrape(browser, baseDirName, articleFileName)
}
