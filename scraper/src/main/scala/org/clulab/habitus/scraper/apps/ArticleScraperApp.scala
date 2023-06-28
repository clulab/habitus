package org.clulab.habitus.scraper.apps

import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import org.clulab.habitus.scraper.corpora.PageCorpus
import org.clulab.habitus.scraper.scrapers.article.CorpusArticleScraper

object ArticleScraperApp extends App {
//  val corpusFileName = args.lift(0).getOrElse("./scraper/articlecorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/thechronicle_articlecorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/threenews_articlecorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/etvghana_articlecorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/happyghana_articlecorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/gna_articlecorpus.txt")
//  val corpusFileName = args.lift(0).getOrElse("./scraper/citifmonline_articlecorpus.txt")
  val corpusFileName = args.lift(0).getOrElse("./scraper/articlecorpus-illegal-mining.txt")
  val baseDirName = args.lift(1).getOrElse("../corpora/scraper-illegal-mining/articles")
  val corpus = PageCorpus(corpusFileName)
  val scraper = new CorpusArticleScraper(corpus)
  val browser: Browser = JsoupBrowser()

  scraper.scrape(browser, baseDirName)
}
