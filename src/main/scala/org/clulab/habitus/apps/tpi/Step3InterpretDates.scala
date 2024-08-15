package org.clulab.habitus.apps.tpi

import org.clulab.habitus.apps.utils.DateString
import org.clulab.habitus.utils.TsvReader
import org.clulab.utils.{FileUtils, Logging, Sourcer}

import scala.util.Using

object Step3InterpretDates extends App with Logging {
  val inputFileName = "../corpora/senegal/stakeholders/senegal-stakeholders-a.tsv"
  val outputFileName = "../corpora/senegal/stakeholders/senegal-stakeholders-b.tsv"
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
        // They were written by Python and don't need to be escaped,
        // especially since we aren't using most of the columns.
        val Array(_, _, date) = tsvReader.readln(line, 3, false)
        val canonicalDate = DateString(date).canonicalize

        println(s"$date -> $canonicalDate")
        printWriter.println(s"$line\t$canonicalDate")
      }
    }
  }
}
