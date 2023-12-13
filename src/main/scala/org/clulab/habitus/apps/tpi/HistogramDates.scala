package org.clulab.habitus.apps.tpi

import org.clulab.utils.{Logging, Sourcer}

import scala.util.Using

object HistogramDates extends App with Logging {
  val inputFileName = "../corpora/uganda/uganda4.tsv"
  val expectedColumnCount = 26
  val years = Using.resource(Sourcer.sourceFromFilename(inputFileName)) { inputSource =>
    val lines = inputSource.getLines
    val firstLine = lines.next
    val years = lines.flatMap { line =>
      val columns = line.split('\t')
//      assert(columns.length == expectedColumnCount)
      val causalIndex = columns(6)

      if (causalIndex == "" || causalIndex == "0") {
        val terms = columns(1).split(' ')
        val canonicalDate = columns(21) // .last

        // If there are multiple matching terms, add that many to the sequence so
        // that we match file counts.
        if (canonicalDate.nonEmpty)
          Seq.fill(1)(canonicalDate.take(4))
        else
          Seq.empty
      }
      else Seq.empty
    }

    years.toVector
  }
  val yearToYears = years.groupBy { year => year }
  val yearToCount = yearToYears.mapValues(_.length)

  yearToCount.keys.toSeq.sorted.foreach { year =>
    println(s"$year\t${yearToCount(year)}")
  }
}
