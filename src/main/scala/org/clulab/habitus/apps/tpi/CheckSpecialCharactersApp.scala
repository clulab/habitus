package org.clulab.habitus.apps.tpi

import org.clulab.utils.{FileUtils, Sourcer}

import scala.collection.mutable.{HashSet => MutableHashSet}
import scala.util.Using

object CheckSpecialCharactersApp extends App {
  val inputFileName = "../corpora/uganda/uganda4.tsv"
  val outputFileName = "../corpora/uganda/uganda4a.tsv"
  val badLetters = MutableHashSet[Char]()

  Using.resource(Sourcer.sourceFromFilename(inputFileName)) { inputSource =>
    Using.resource(FileUtils.printWriterFromFile(outputFileName)) { printWriter =>
      val lines = inputSource.getLines()

      lines.foreach { line =>
        val fixedLine = line.map { letter =>
          // Visual Studio Code does not like the letter 0x2028 (8232).
          // It is a line separator (LS) character.  I guess it is OK to
          // leave it in the file until it is republished.
          // PS (paragraph separator), 0x2029 (8233) is also a problem.
          if (8232 <= letter.toInt && letter.toInt < 8234) {
            badLetters += letter
            ' '
          }
          else
            letter
        }

        printWriter.println(fixedLine)
      }
    }
  }
  println("Here's the set of bad letters:")
  badLetters.toSeq.sorted.foreach { letter =>
    println(s"$letter\t${letter.toInt}")
  }
}
