package org.clulab.habitus.apps.grid

import org.clulab.utils.{FileUtils, Sourcer}

import java.io.PrintWriter
import scala.util.Using

object JoinDatasetsApp extends App {
  val inputFileNames = Seq(
    "../corpora/ghana-elasticsearch/ghana-elasticsearch-0.tsv",
    "../corpora/ghana-elasticsearch/ghana-elasticsearch-1.tsv",
    "../corpora/ghana-elasticsearch/ghana-elasticsearch-2.tsv",
    "../corpora/ghana-elasticsearch/ghana-elasticsearch-3.tsv",
    "../corpora/ghana-elasticsearch/ghana-elasticsearch-4.tsv"
  )
  val ouputFileName = "../corpora/ghana-elasticsearch/combined.tsv"

  def copyLines(inputFileName: String, printWriter: PrintWriter, keepHeader: Boolean): Unit = {
    Using.resource(Sourcer.sourceFromFilename(inputFileName)) { source =>
      val lines = source.getLines.drop(if (keepHeader) 0 else 1)

      lines.foreach(printWriter.println)
    }
  }

  Using.resource(FileUtils.printWriterFromFile(ouputFileName)) { printWriter =>
    inputFileNames.zipWithIndex.foreach { case (inputFileName, index) =>
      copyLines(inputFileName, printWriter, index == 0)
    }
  }
}
