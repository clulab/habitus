package org.clulab.habitus.apps.tpi

import org.clulab.utils.{FileUtils, Logging, Sourcer, Unordered}
import org.clulab.utils.Unordered.OrderingOrElseBy

import java.net.URL
import scala.util.Using

case class FullKey(year: String, source: String, term: String) {

  override def toString: String = s"$year\t$source\t$term"
}

case class PartialKey(year: String, source: String) {

  override def toString: String = s"$year\t$source"
}

object HistogramDatesSourcesTerms extends App with Logging {
  implicit val fullKeyOrder = {
    Unordered[FullKey]
      .orElseBy(_.year)
      .orElseBy(_.source)
      .orElseBy(_.term)
  }
  implicit val partialKeyOrder = {
    Unordered[PartialKey]
      .orElseBy(_.year)
      .orElseBy(_.source)
  }

  val inputFileName = "../corpora/uganda/uganda4.tsv"
  // If there are missing locations at the end of the record,
  // the last columns can be truncated, so one should not use
  // columns.last to grab the canonicalDate.
  var count = 0
  val fullAndPartialKeysAndUrl = Using.resource(Sourcer.sourceFromFilename(inputFileName)) { inputSource =>
    val lines = inputSource.getLines
    val firstLine = lines.next
    val fullAndPartialKeys = lines.map { line =>
      val columns = line.split('\t')
      val canonicalDate = columns(21)
      val yearOpt =
          if (canonicalDate.nonEmpty) Some(canonicalDate.take(4))
          else None
      val url = columns(0)
      val source = new URL(url).getHost
      // There are instances of e.g., "uganda china".
      val terms = columns(1).split(' ').filterNot(_ == "uganda")
      val fullKeys = yearOpt.map { year =>
        val keys = terms.map { term =>
          FullKey(year, source, term)
        }

        keys.toSeq
      }
      .getOrElse(Seq.empty[FullKey])
      val partialKeys = yearOpt.map { year =>
        Seq(PartialKey(year, source))
      }.getOrElse(Seq.empty[PartialKey])

      (fullKeys, partialKeys, url)
    }

    fullAndPartialKeys.toVector
  }
  val fullKeys = fullAndPartialKeysAndUrl.flatMap { case (fullKeys, partialKeys, _) =>
    fullKeys
  }
  val partialKeys = fullAndPartialKeysAndUrl.flatMap { case (fullKeys, partialKeys, _) =>
    partialKeys
  }
  val urls = fullAndPartialKeysAndUrl.map { case (fullKeys, partialKeys, url) =>
    url
  }.distinct

  println(s"There are ${urls.length} distinct URLs.")

  val fullKeyToKeys = fullKeys.groupBy { key => key }
  val fullKeyToCount = fullKeyToKeys.mapValues(_.length)

//  fullKeyToCount.keys.toSeq.sorted.foreach { key =>
//    println(s"$key\t${fullKeyToCount(key)}")
//  }

  val partialKeyToKeys = partialKeys.groupBy { key => key }
  val partialKeyToCount = partialKeyToKeys.mapValues(_.length)

  val terms = fullKeys.map(_.term).distinct.sorted
  terms.foreach { term =>
    val counts = fullKeyToCount.filterKeys { fullKey =>
      fullKey.term == term
    }.values
    val count = counts.sum

    println(s"$term\t$count")
  }

  val years = partialKeys.map(_.year).distinct.sorted
  years.foreach { year =>
    val counts = partialKeyToCount.filterKeys { partialKey =>
      partialKey.year == year
    }.values
    val count = counts.sum

    println(s"$year\t$count")
  }

  val sources = partialKeys.map(_.source).distinct.sorted
  sources.foreach { source =>
    val counts = partialKeyToCount.filterKeys { partialKey =>
      partialKey.source == source
    }.values
    val count = counts.sum

    println(s"$source\t$count")
  }


}
