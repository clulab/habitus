package org.clulab.habitus.apps.tpi

import org.clulab.utils.{Logging, Sourcer}

import scala.util.Using

object HistogramLocations extends App with Logging {
  val inputFileName = "../corpora/uganda/uganda3.tsv"
  val expectedColumnCount = 22
  val locations = Using.resource(Sourcer.sourceFromFilename(inputFileName)) { inputSource =>
    val lines = inputSource.getLines
    val _ = lines.next
    val locations = lines.flatMap { line =>
      val columns = line.split('\t')
      assert(columns.length == expectedColumnCount)
      val location = columns(19)

      if (location.isEmpty) Seq.empty
      else {
        val recoded = location.replaceAllLiterally("), ", ");")
        val split = recoded.split(';')

        split.toSeq
      }
    }

    locations.toVector
  }
  val grouped = locations.groupBy(location => location)
  val counted = grouped.map { case (location, locations) => location -> locations.length }.toVector
  val sorted = counted.sortBy { case (location, count) => (count, location) }.reverse

  sorted.foreach(println)
}
