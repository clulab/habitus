package org.clulab.habitus.scraper.apps

import net.ruippeixotog.scalascraper.browser.Browser
import org.clulab.habitus.scraper.browsers.HabitusBrowser
import org.clulab.habitus.scraper.corpora.PageCorpus
import org.clulab.habitus.scraper.scrapers.index.CorpusIndexScraper

/**
  * Take the list of URLs in corpusFileName, find the downloaded pages, scrape them,
  * and put the links found into articleFileName so that they can be downloaded and
  * later scraped themselves.
  */

object IndexScraperApp extends App {
//  val corpusFileName = args.lift(0).getOrElse("./scraper/indexcorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/thechronicle_indexcorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/threenews_indexcorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/etvghana_indexcorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/happyghana_indexcorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/gna_indexcorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/adomonline_indexcorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/citifmonline_indexcorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/indexcorpus-illegal-mining.txt")
  val corpusFileName = args.lift(0).getOrElse("./scraper/corpora/multi2/harvest/indexcorpus.txt")
//  val articleFileName = args.lift(1).getOrElse("./scraper/articlecorpus.txt")
//  val articleFileName = args.lift(1).getOrElse("./scraper/thechronicle_articlecorpus.txt")
//  val articleFileName = args.lift(1).getOrElse("./scraper/threenews_articlecorpus.txt")
//  val articleFileName = args.lift(1).getOrElse("./scraper/etvghana_articlecorpus.txt")
//  val articleFileName = args.lift(1).getOrElse("./scraper/happyghana_articlecorpus.txt")
//  val articleFileName = args.lift(1).getOrElse("./scraper/gna_articlecorpus.txt")
//  val articleFileName = args.lift(1).getOrElse("./scraper/adomonline_articlecorpus.txt")
//  val articleFileName = args.lift(1).getOrElse("./scraper/citifmonline_articlecorpus.txt")
//  val articleFileName = args.lift(1).getOrElse("./scraper/articlecorpus-illegal-mining.txt")
  val articleFileName = args.lift(1).getOrElse("./scraper/corpora/multi2/harvest/articlecorpus.txt")

  val baseDirName = args.lift(1).getOrElse("../corpora/multi2/harvest/indexes")
  val corpus = PageCorpus(corpusFileName)
  val scraper = new CorpusIndexScraper(corpus)
  val browser: Browser = new HabitusBrowser()

  scraper.scrape(browser, baseDirName, articleFileName)
}
