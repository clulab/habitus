package org.clulab.habitus.apps.tpi

import org.clulab.utils.{FileUtils, Logging, Sourcer}
import org.clulab.wm.eidoscommon.utils.TsvReader

import scala.util.Using

object Step4FindNearestLocation extends App with Logging {

  case class LocationAndIndex(location: String, index: Int)

  case class LocationAndDistance(location: String, distance: Int) {

    override def toString: String = {
      s"$location\t$distance"
    }
  }

  case class LineLocationAndDistance(prevLocationAndDistanceOpt: Option[LocationAndDistance], nextLocationAndDistanceOpt: Option[LocationAndDistance]) {

    override def toString: String = {
      def toString(locationAndDistanceOpt: Option[LocationAndDistance]): String = {
        locationAndDistanceOpt.map(_.toString).getOrElse("\t")
      }

      s"${toString(prevLocationAndDistanceOpt)}\t${toString(nextLocationAndDistanceOpt)}"
    }
  }

  object LineLocationAndDistance {
    val header = "prevLocation\tprevDistance\tnextLocation\tnextDistance"
  }

  val inputFileName = "../corpora/uganda/uganda3.tsv"
  val outputFileName = "../corpora/uganda/uganda4.tsv"
  val expectedColumnCount = 22
  val tsvReader = new TsvReader()
  var articleIndex = 0

  def getLineLocationAndDistances(lines: Seq[String]): Seq[LineLocationAndDistance] = {
    val locationAndIndexes = lines.map { line =>
      val columns = tsvReader.readln(line)
      val sentenceIndex = columns(3).toInt
      val sentenceLocation = columns(19)

      LocationAndIndex(sentenceLocation, sentenceIndex)
    }.toList

    @annotation.tailrec
    def getNearestLocationAndDistances(locationAndIndexes: List[LocationAndIndex], nearestLocationAndIndexOpt: Option[LocationAndIndex], nearestLocationAndDistanceOpts: List[Option[LocationAndDistance]]): List[Option[LocationAndDistance]] = {
      if (locationAndIndexes.isEmpty)
        nearestLocationAndDistanceOpts
      else {
        val locationAndIndex = locationAndIndexes.head

        if (locationAndIndex.location.nonEmpty)
          // There is a new nearest one.
          getNearestLocationAndDistances(locationAndIndexes.tail, Some(locationAndIndex),
              Some(LocationAndDistance(locationAndIndex.location, 0)) :: nearestLocationAndDistanceOpts)
        else
          // We need one in the current row.
          if (nearestLocationAndIndexOpt.isDefined)
            // We have one available.
            getNearestLocationAndDistances(locationAndIndexes.tail, nearestLocationAndIndexOpt,
                Some(LocationAndDistance(nearestLocationAndIndexOpt.get.location, math.abs(locationAndIndex.index - nearestLocationAndIndexOpt.get.index))) :: nearestLocationAndDistanceOpts)
          else
            // We need one, but there is none available.
            getNearestLocationAndDistances(locationAndIndexes.tail, nearestLocationAndIndexOpt,
                None :: nearestLocationAndDistanceOpts)
      }
    }

    val prevLocationAndDistanceOpts = getNearestLocationAndDistances(locationAndIndexes, None, List.empty).reverse
    val nextLocationAndDistanceOpts = getNearestLocationAndDistances(locationAndIndexes.reverse, None, List.empty)
    val lineLocationAndDistances = prevLocationAndDistanceOpts.zip(nextLocationAndDistanceOpts).map { case (prevLocationAndDistanceOpt, nextLocationAndDistanceOpt) =>
      LineLocationAndDistance(prevLocationAndDistanceOpt, nextLocationAndDistanceOpt)
    }

    lineLocationAndDistances
  }

  def checkline(line: String): String = {
    val columnCount = line.count(_ == '\t') + 1

    assert(columnCount == expectedColumnCount)
    line
  }

  Using.resource(Sourcer.sourceFromFilename(inputFileName)) { inputSource =>
    Using.resource(FileUtils.printWriterFromFile(outputFileName)) { printWriter =>
      val lines = inputSource.getLines.buffered
      val firstLine = checkline(lines.next)

      printWriter.println(s"$firstLine\t${LineLocationAndDistance.header}")

      while (lines.hasNext) {
        val headArticleLine = checkline(lines.next)
        val headArticleUrl = tsvReader.readln(headArticleLine, 1).head

        println(s"$articleIndex\t$headArticleUrl")
        articleIndex += 1

        @annotation.tailrec
        def takeArticleLines(articleLines: List[String]): List[String] = {
          if (
            lines.hasNext && {
              val tailArticleLine = checkline(lines.head)
              val tailArticleUrl = tsvReader.readln(tailArticleLine, 1).head

              tailArticleUrl == headArticleUrl
            }
          )
            takeArticleLines(lines.next() :: articleLines)
          else
            articleLines
        }

        val articleLines = takeArticleLines(List(headArticleLine)).reverse.toVector
        val articleLocations = getLineLocationAndDistances(articleLines)

        articleLines.zip(articleLocations).foreach { case (line, locations) =>
          val count = locations.toString.count(_ == '\t')
          if (count != 3)
            println("Why?")

          printWriter.println(s"$line\t$locations")
        }
      }
    }
  }
}
