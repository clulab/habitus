package org.clulab.habitus.apps.tpi

import org.clulab.utils.{Logging, Sourcer}

import scala.util.Using

object Step4HistogramDates extends App with Logging {
  val inputFileName = "../corpora/multi/CausalBeliefsDate.tsv"
  val expectedColumnCount = 22
  val years = Using.resource(Sourcer.sourceFromFilename(inputFileName)) { inputSource =>
    val lines = inputSource.getLines
    val firstLine = lines.next
    val years = lines.flatMap { line =>
      val columns = line.split('\t')
      assert(columns.length == expectedColumnCount)
      val canonicalDate = columns.last

      if (canonicalDate.nonEmpty) Some(canonicalDate.take(4))
      else None
    }

    years.toVector
  }
  val yearToYears = years.groupBy { year => year }
  val yearToCount = yearToYears.mapValues(_.length)

  yearToCount.keys.toSeq.sorted.foreach { year =>
    println(s"$year\t${yearToCount(year)}")
  }
}
