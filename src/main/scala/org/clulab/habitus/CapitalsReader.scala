package org.clulab.habitus

import org.clulab.habitus.utils.EqualityHashBag
import org.clulab.utils.FileUtils

object CapitalsReader extends App {
  val inputDir: String = args(0)
  val processor = new HabitusProcessor(None)
  val files = FileUtils.findFiles(inputDir, ".txt")

  files.foreach { file =>
    val text = FileUtils.getTextFromFile(file)
    val document = processor.mkDocument(text)
    val wordsAndValids = document.sentences.flatMap { sentence =>
      val valids = sentence.words.scanLeft(false) { case (lowerSeen, word) =>
        lowerSeen || (word.head.isLetter && word.head.isLower)
      }.drop(1)

      sentence.words.zip(valids)
    }
    val validWords = wordsAndValids.filter(_._2).map(_._1)
    val allUpperToWords = validWords.groupBy { word =>
      // This will skip words with non-letters.
      word.forall { letter => letter.isUpper }
    }
    val allUpperBag = EqualityHashBag(allUpperToWords(true))
    val notAllUpperBag = EqualityHashBag(allUpperToWords(false).map(_.toUpperCase))
    val allUpperToCounts = allUpperBag.map { allUpper =>
      allUpper -> (allUpperBag.count(allUpper), notAllUpperBag.count(allUpper))
    }.toMap
    val allUpperToGt2Eq0 = allUpperToCounts.filter { case (_, (allUpperCount, notAllUpperCount)) =>
      allUpperCount > 1 && notAllUpperCount == 0
    }

    val allUpperAndCount = allUpperToGt2Eq0.map { case (allUpper, (allUpperCount, notAllUpperCount)) =>
      (allUpper, allUpperCount)
    }.toSeq.sortBy { case (allUpper, allUpperCount) => (-allUpperCount, allUpper) }

    println(s"\nFile: ${file.getName}\n")
    allUpperAndCount.foreach { case (allUpper, count) =>
      println(s"$allUpper\t$count")
    }
  }
}
