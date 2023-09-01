package org.clulab.habitus.apps.tpi

import org.clulab.utils.{Logging, Sourcer, Unordered}
import org.clulab.utils.Unordered.OrderingOrElseBy

import java.net.URL
import scala.util.Using

case class Key(year: String, source: String, term: String) {

  override def toString: String = s"$year\t$source\t$term"
}

object HistogramDatesSourcesTerms extends App with Logging {
  implicit val keyOrder = {
    Unordered[Key]
      .orElseBy(_.year)
      .orElseBy(_.source)
      .orElseBy(_.term)
  }

  val inputFileName = "../corpora/multi/CausalBeliefsDate.tsv"
  val expectedColumnCount = 22
  val keys = Using.resource(Sourcer.sourceFromFilename(inputFileName)) { inputSource =>
    val lines = inputSource.getLines
    val firstLine = lines.next
    val keys = lines.flatMap { line =>
      val columns = line.split('\t')
      assert(columns.length == expectedColumnCount)
      val canonicalDate = columns.last
      val yearOpt =
          if (canonicalDate.nonEmpty) Some(canonicalDate.take(4))
          else None
      val source = new URL(columns(0)).getHost
      val terms = columns(1).split(' ')
      val keys = yearOpt.map { year =>
        val keys = terms.map { term =>
          Key(year, source, term)
        }

        keys.toSeq
      }
      .getOrElse(Seq.empty[Key])

      keys
    }

    keys.toVector
  }
  val keyToKeys = keys.groupBy { key => key }
  val keyToCount = keyToKeys.mapValues(_.length)

  keyToCount.keys.toSeq.sorted.foreach { key =>
    println(s"$key\t${keyToCount(key)}")
  }
}
