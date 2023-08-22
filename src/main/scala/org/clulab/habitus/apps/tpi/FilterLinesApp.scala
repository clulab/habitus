package org.clulab.habitus.apps.tpi

import org.clulab.utils.{FileUtils, Sourcer}

import scala.io.Source
import scala.util.Using

object FilterLinesApp extends App {
  val inputFileName = "../corpora/multi-old/outputCausalBeliefs-all.tsv"
  val outputFileName = "../corpora/multi-old/outputCausalBeliefs-nogw.tsv"

  Using.resource(FileUtils.printWriterFromFile(outputFileName)) { printWriter =>
    Using.resource(Sourcer.sourceFromFilename(inputFileName)) { source =>
      val lines = source.getLines()
      val goodLines = lines.filterNot(_.startsWith("https://www.ghanaweb.com/"))

      goodLines.foreach(printWriter.println)
    }
  }
}
