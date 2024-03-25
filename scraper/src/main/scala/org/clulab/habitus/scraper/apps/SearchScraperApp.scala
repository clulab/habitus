package org.clulab.habitus.scraper.apps

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.browsers.HabitusBrowser
import org.clulab.habitus.scraper.corpora.SearchCorpus
import org.clulab.habitus.scraper.scrapers.search.CorpusSearchScraper

/**
  * Take the list of URLs in corpusFileName, find the downloaded pages, scrape them,
  * and put the links found into articleFileName so that they can be downloaded and
  * later scraped themselves.
  */

object SearchScraperApp extends App {
  val term = "sitemap"
  val corpusFileName = args.lift(0).getOrElse(s"./scraper/corpora/ghana/$term/searchcorpus.txt")
  val articleFileName = args.lift(1).getOrElse(s"./scraper/corpora/ghana/$term/indexcorpus.txt")
  val baseDirName = args.lift(2).getOrElse("../corpora/ghana/sitemap/searches")
  val corpus = SearchCorpus(corpusFileName)
  val scraper = new CorpusSearchScraper(corpus)
  val browser: Browser = new HabitusBrowser()

  scraper.scrape(browser, baseDirName, articleFileName)
}
