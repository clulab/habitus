package org.clulab.habitus.scraper.apps

import org.clulab.utils.{FileUtils, Sourcer, StringUtils}

import scala.util.Using

object FilterLogApp extends App {
  val inFileName = args.lift(0).getOrElse("/home/kwa/Projects/clulab/habitus-project/habitus/ArticleScraperApp-ghanaweb.log")
  val outFileName = args.lift(1).getOrElse("/home/kwa/Projects/clulab/habitus-project/habitus/articlecorpus.txt")

  Using.resources(
    FileUtils.printWriterFromFile(outFileName),
    Sourcer.sourceFromFilename(inFileName)
  ) { (printWriter, source) =>
    val lines = source.getLines
    val filteredLines = lines.filter { line =>
      line.endsWith(" failed!")
    }

    filteredLines.foreach { line =>
      val newLine = line
          .drop("Scrape of ".length)
          .dropRight(" failed!".length)

      printWriter.println(newLine)
      println(newLine)
    }
  }
}
