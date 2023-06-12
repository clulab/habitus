package org.clulab.habitus.scraper.apps

import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import org.clulab.habitus.scraper.Corpus
import org.clulab.habitus.scraper.scrapers.article.CorpusArticleScraper

object ArticleScraperApp extends App {
  val corpusFileName = args.lift(0).getOrElse("./scraper/articlecorpus.txt")
  val baseDirName = args.lift(1).getOrElse("../corpora/scraper/articles")
  val corpus = Corpus(corpusFileName)
  val scraper = new CorpusArticleScraper(corpus)
  val browser: Browser = JsoupBrowser()

  scraper.scrape(browser, baseDirName)
}