package org.clulab.habitus.apps.grid

import org.clulab.utils.{FileUtils, Sourcer}

import java.io.PrintWriter
import scala.util.Using

object CombineDatasetsApp extends App {
  val inputFileNames = Seq(
    "../corpora/grid/uganda.tsv",
    "../corpora/grid/uganda-mining2.tsv",
    "../corpora/grid/uganda-pdfs2.tsv",
    "../corpora/grid/uganda-pdfs-karamoja2.tsv"
  )
  val ouputFileName = "../corpora/grid/uganda-all.tsv"

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
