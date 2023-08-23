package org.clulab.habitus.apps.tpi

import org.clulab.utils.{FileUtils, Sourcer}

import scala.util.Using

object CheckColumnsApp extends App {
  val inputFileName = "../corpora/multi/CausalBeliefsEven2.tsv"
  val outputFileName = "../corpora/multi/CausalBeliefsEven3.tsv"
  val expectedColumnCount = 21

  Using.resource(Sourcer.sourceFromFilename(inputFileName)) { inputSource =>
    Using.resource(FileUtils.printWriterFromFile(outputFileName)) { printWriter =>
      val lines = inputSource.getLines()
      val firstLine = lines.next()

      def loop(text: String, cumulatedColumnCount: Int): String = {
        assert(lines.hasNext)
        val nextLine = lines.next()
        val nextColumnCount = cumulatedColumnCount + nextLine.count(_ == '\t')
        val nextText = if (text.isEmpty) nextLine else s"$text\n$nextLine"

        if (expectedColumnCount == nextColumnCount)
          nextText
        else if (expectedColumnCount < nextColumnCount)
          throw new RuntimeException("Bad column count!")
        else
          loop(nextText, nextColumnCount)
      }

      printWriter.println(firstLine)
      while (lines.hasNext) {
        val completeLine = loop("", 1) // N separators create N + 1 columns.
        val splicedLine = completeLine.replace('\n', ' ')
        val controlCount = splicedLine.count(_.toInt < 32)
        val uncontrolledLine =
            if (expectedColumnCount <= controlCount) {
              splicedLine.map { letter =>
                if (letter.toInt < 32 && letter != '\t')
                  ' '
                else letter
              }
            }
            else
              splicedLine
        val fixedLine = uncontrolledLine

        if (completeLine != fixedLine)
          println("This one was fixed")

        if (!fixedLine.startsWith("url"))
          printWriter.println(fixedLine)
        else
          println("Skipped extra header!")
      }
    }
  }
}
