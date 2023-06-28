package org.clulab.habitus.scraper.corpora

import org.clulab.utils.Sourcer

import scala.util.Using

trait Corpus[T] {

  def items: Seq[T]

}

object Corpus {

  def getLines(fileName: String): Seq[String] = {
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
