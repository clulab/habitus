package org.clulab.habitus.apps.tpi

import org.clulab.habitus.utils.TsvReader
import org.clulab.utils.{FileUtils, Logging, Sourcer}

import scala.util.Using

object FindLocationsApp extends App with Logging {
  val inputFileName = "../corpora/uganda-tsv/uganda.tsv"
  val tsvReader = new TsvReader()
  val sentenceLocationsColumn = 19
  val sentenceColumn = 4
  val location = "India \\(2\\.18333, 33\\.68333\\)(, ?)?"

  Using.resource(Sourcer.sourceFromFilename(inputFileName)) { inputSource =>
    val lines = inputSource.getLines.buffered
    val firstLine = lines.next

    while (lines.hasNext) {
      val line = lines.next
      val columns = line.split('\t')
      val isLocation = columns.lift(sentenceLocationsColumn)
        .exists(_.matches(location))

      if (isLocation)
        println(columns(sentenceColumn))
    }
  }
}
