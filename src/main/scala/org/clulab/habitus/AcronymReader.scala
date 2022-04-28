package org.clulab.habitus

import org.clulab.habitus.utils.EqualityHashBag
import org.clulab.processors.Processor
import org.clulab.utils.FileUtils

object CapitalsReader extends App {
  val inputDir: String = args(0)
  val processor: HabitusProcessor = new HabitusProcessor(None)
  val files = FileUtils.findFiles(inputDir, ".txt")

  def getAllUpperAcronyms(processor: Processor, text: String): Seq[(String, Int)] = {
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
      word.forall { letter => letter.isLetter && letter.isUpper }
    }
    val allUpperBag = EqualityHashBag(allUpperToWords(true))
    val notAllUpperBag = EqualityHashBag(allUpperToWords(false).map(_.toUpperCase))
    val allUpperToCounts = allUpperBag.map { allUpper =>
      allUpper -> (allUpperBag.count(allUpper), notAllUpperBag.count(allUpper))
    }.toMap
    val allUpperToGt2Eq0 = allUpperToCounts.filter { case (_, (allUpperCount, notAllUpperCount)) =>
      allUpperCount > 1 && notAllUpperCount == 0
    }

    val allUpperAndCount = allUpperToGt2Eq0.map { case (allUpper, (allUpperCount, _)) =>
      (allUpper, allUpperCount)
    }.toSeq.sortBy { case (allUpper, allUpperCount) => (-allUpperCount, allUpper) }

    allUpperAndCount
  }

  def getInitialUpperAcronyms(processor: Processor, text: String): Seq[(String, Int)] = {
    val document = processor.mkDocument(text)
    val wordsAndValids = document.sentences.flatMap { sentence =>
      val valids = sentence.words.scanLeft(false) { case (lowerSeen, word) =>
        lowerSeen || (word.head.isLetter && word.head.isLower)
      }.drop(1)

      sentence.words.zip(valids)
    }
    val validWords = wordsAndValids.filter(_._2).map(_._1)
    val initialUpperWords = validWords.groupBy { word =>
      // This will skip words with non-letters.
      word.head.isLetter && word.head.isUpper &&
      word.drop(1).forall { letter => letter.isLetter && letter.isLower }
    }
    val initialUpperBag = EqualityHashBag(initialUpperWords(true))
    val notInitialUpperBag = EqualityHashBag(initialUpperWords(false).map { word =>
      val letters = word.zipWithIndex.map { case (letter, index) =>
        if (index == 0) letter.toUpper
        else letter.toLower
      }

      "" ++ letters
    })
    val allUpperToCounts = initialUpperBag.map { allUpper =>
      allUpper -> (initialUpperBag.count(allUpper), notInitialUpperBag.count(allUpper))
    }.toMap
    val allUpperToGt2Eq0 = allUpperToCounts.filter { case (_, (allUpperCount, notAllUpperCount)) =>
      allUpperCount > 1 && notAllUpperCount == 0
    }

    val allUpperAndCount = allUpperToGt2Eq0.map { case (allUpper, (allUpperCount, _)) =>
      (allUpper, allUpperCount)
    }.toSeq.sortBy { case (allUpper, allUpperCount) => (-allUpperCount, allUpper) }

    allUpperAndCount
  }

  files.foreach { file =>
    val text = FileUtils.getTextFromFile(file)
    val allUpperAndCount = getAllUpperAcronyms(processor, text)
    val initialUpperAndCount = getInitialUpperAcronyms(processor, text)

    println(s"\nFile: ${file.getName}\n")
    allUpperAndCount.foreach { case (allUpper, count) =>
      println(s"$allUpper\t$count")
    }
    initialUpperAndCount.foreach { case (allUpper, count) =>
      println(s"$allUpper\t$count")
    }
  }
}
