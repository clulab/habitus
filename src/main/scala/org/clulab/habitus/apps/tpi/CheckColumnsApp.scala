package org.clulab.habitus.apps.tpi

import org.clulab.utils.{Logging, Sourcer}

import scala.util.Using

object CheckColumnsApp extends App with Logging {
  val inputFileName = "../corpora/ghana-output/ghana-larger-regulations.tsv"
  val expectedColumnCount = 26

  Using.resource(Sourcer.sourceFromFilename(inputFileName)) { inputSource =>
    val lines = inputSource.getLines

    lines.zipWithIndex.foreach { case (line, index) =>
      val columnCount = line.count(_ == '\t') + 1

      if (columnCount != expectedColumnCount)
        println(s"Line $index had $columnCount lines when $expectedColumnCount were expected.")
    }
  }
}
