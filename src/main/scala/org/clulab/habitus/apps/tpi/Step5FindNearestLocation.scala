package org.clulab.habitus.apps.tpi

import org.clulab.utils.{FileUtils, Logging, Sourcer}
import org.clulab.wm.eidoscommon.utils.TsvReader

import scala.util.Using

case class LineLocation(prevLocation: String, prevDistance: Int, nextLocation: String, nextDistance: Int) {

  override def toString: String = {
    s"$prevLocation\t$prevDistance\t$nextLocation\t$nextDistance"
  }
}

object LineLocation {
  val header = "prevLocation\tprevDistance\tnextLocation\tnextDistance"
}

object Step5FindNearestLocation extends App with Logging {
  val inputFileName = "../corpora/multimix/dataset100.tsv"
  val outputFileName = "../corpora/multimix/dataset55knearest.tsv"
  val expectedColumnCount = 22


  def getLineLocations(lines: Seq[String]): Seq[LineLocation] = {
    val lineLocations = lines.map { line =>
      LineLocation("one", 1, "two", 2)
    }

    lineLocations
  }

  def checkline(line: String): String = {
    val columnCount = line.count(_ == '\t') + 1

    assert(columnCount == expectedColumnCount)
    line
  }

  Using.resource(Sourcer.sourceFromFilename(inputFileName)) { inputSource =>
    Using.resource(FileUtils.printWriterFromFile(outputFileName)) { printWriter =>
      val tsvReader = new TsvReader()
      val lines = inputSource.getLines.buffered
      val firstLine = checkline(lines.next)

      printWriter.println(s"$firstLine\t${LineLocation.header}")

      while (lines.hasNext) {
        val headArticleLine = checkline(lines.next)
        val headArticleUrl = tsvReader.readln(headArticleLine, 1).head

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
        val articleLocations = getLineLocations(articleLines)

        articleLines.zip(articleLocations).foreach { case (line, locations) =>
          printWriter.println(s"$line\t$locations")
        }
      }
    }
  }
}
