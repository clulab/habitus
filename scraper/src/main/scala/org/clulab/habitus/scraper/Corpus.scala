package org.clulab.habitus.scraper

import org.clulab.utils.Sourcer

import scala.util.Using

class Corpus(val fileName: String) {
  // This may not always be the case
  val lines: Seq[String] = getLines()
  val pages: Seq[Page] = lines.map(Page(_))

  def getLines(): Seq[String] = {
    val lines = Using.resource(Sourcer.sourceFromFilename(fileName)) { source =>
      source
        .getLines()
        .map(_.trim)
        .filter(_.nonEmpty)
        .filterNot(_.startsWith("#"))
        .toVector
    }

    lines
  }
}

object Corpus {

  def apply(fileName: String): Corpus = {
    new Corpus(fileName)
  }
}
