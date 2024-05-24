package org.clulab.habitus.apps.tpi

import org.clulab.habitus.utils.TsvReader
import org.clulab.utils.{FileUtils, Logging, Sourcer}

import scala.util.Using

object PatchLocationsApp extends App with Logging {
  val inputFileName = "../corpora/uganda-tsv/uganda-sneakpeek.tsv"
  val outputFileName = "../corpora/uganda-tsv/uganda-sneakpeek1.tsv"
  val expectedColumnCount = 26
  val minimumColumnCount = 22
  val tsvReader = new TsvReader()
  val sentenceLocationsColumn = 19
  val contextLocationsColumn = 20
  val badKaramoja = "Karamoja \\(nan, nan\\)"
  val goodKaramoja = "Karamoja (2.53453, 34.66659)"
  val badIndia = "India \\(2\\.18333, 33\\.68333\\)(, ?)?"
  val goodIndia = ""

  def checkline(line: String): String = {
    val columnCount = line.count(_ == '\t') + 1

    if (columnCount != expectedColumnCount)
      if (columnCount < minimumColumnCount)
        println(s"short line: $line")
    line
  }

  def lineToColumns(line: String): Array[String] = {
    val columns = line.split('\t').take(minimumColumnCount)
    val columnsToAdd = math.max(0, minimumColumnCount - columns.length)
    val filledColumns =
        if (columnsToAdd == 0) columns
        else columns ++ Array.fill(columnsToAdd)("")

    filledColumns
  }

  def columnsToString(columns: Array[String]): String = {
    val string = columns.mkString("\t")

    string
  }

  Using.resource(Sourcer.sourceFromFilename(inputFileName)) { inputSource =>
    Using.resource(FileUtils.printWriterFromFile(outputFileName)) { printWriter =>
      val lines = inputSource.getLines.buffered
      val firstLine = checkline(lines.next)
      val firstString = columnsToString(lineToColumns(firstLine))

      printWriter.println(firstString)

      while (lines.hasNext) {
        val line = checkline(lines.next)
        val columns = lineToColumns(line)
        val sentenceLocations = columns(sentenceLocationsColumn)
        val contextLocations = columns(contextLocationsColumn)

        val betterSentenceLocations = sentenceLocations
            .replaceAll(badKaramoja, goodKaramoja)
            .replaceAll(badIndia, goodIndia)
        val betterContextLocations = contextLocations
            .replaceAll(badKaramoja, goodKaramoja)
            .replaceAll(badIndia, goodIndia)

        columns.update(sentenceLocationsColumn, betterSentenceLocations)
        columns.update(contextLocationsColumn, betterContextLocations)

        val newString = columnsToString(columns)

        printWriter.println(newString)
      }
    }
  }
}
