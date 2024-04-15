package org.clulab.habitus.apps.tpi

import org.clulab.habitus.apps.utils.DateString
import org.clulab.utils.{FileUtils, Logging, Sourcer}
import org.clulab.wm.eidoscommon.utils.TsvReader

import scala.util.Using

object Step3InterpretDates extends App with Logging {
  val inputFileName = "../corpora/ghana-set/set/set-a.tsv"
  val outputFileName = "../corpora/ghana-set/set/set-b.tsv"
  val expectedColumnCount = 22

  Using.resource(Sourcer.sourceFromFilename(inputFileName)) { inputSource =>
    Using.resource(FileUtils.printWriterFromFile(outputFileName)) { printWriter =>
      val tsvReader = new TsvReader()
      val lines = inputSource.getLines
      val firstLine = lines.next

      printWriter.println(s"$firstLine\tcanonicalDate")
      lines.foreach { line =>
        val columnCount = line.count(_ == '\t') + 1
        assert(columnCount == expectedColumnCount)
        val Array(_, _, date) = tsvReader.readln(line, 3)
        val canonicalDate = DateString(date).canonicalize

        println(s"$date -> $canonicalDate")
        printWriter.println(s"$line\t$canonicalDate")
      }
    }
  }
}
