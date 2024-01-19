package org.clulab.habitus.scraper.apps

import org.clulab.habitus.scraper.scrapers.article.GoogleArticleScraper

import java.io.File

object ScrapePdfApp extends App {
  val pdfFileName = args.lift(0).getOrElse("../corpora/test/test.pdf")
  val pdfFile = new File(pdfFileName)

  if (!pdfFile.exists)
    println(s"""The file "$pdfFileName" does not exist!""")
  val rawText = GoogleArticleScraper.pdf2txt.read(pdfFile, None)

  println(rawText)
}
