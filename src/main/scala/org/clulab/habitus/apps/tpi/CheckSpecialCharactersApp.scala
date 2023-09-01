package org.clulab.habitus.apps.tpi

import org.clulab.utils.{FileUtils, Sourcer}

import scala.collection.mutable.{HashSet => MutableHashSet}
import scala.util.Using

object CheckSpecialCharactersApp extends App {
  val inputFileName = "../corpora/multi/CausalBeliefsDate.tsv"
  val outputFileName = "../corpora/multi/CausalBeliefsDateClean.tsv"
  val badLetters = MutableHashSet[Char]()

  Using.resource(Sourcer.sourceFromFilename(inputFileName)) { inputSource =>
    Using.resource(FileUtils.printWriterFromFile(outputFileName)) { printWriter =>
      val lines = inputSource.getLines()

      lines.foreach { line =>
        val fixedLine = line.map { letter =>
          // Visual Studio Code does not like this letter, 0x2028.
          // It is a line separator character.  I guess it is OK to
          // leave it in the file until it is republished.
          if (8232 <= letter.toInt && letter.toInt < 8233) {
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
