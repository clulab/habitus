package org.clulab.habitus

import org.clulab.habitus.utils.EqualityHashBag
import org.clulab.processors.Processor
import org.clulab.utils.FileUtils

object AcronymReader extends App {
  val inputDir: String = args(0)
  val processor: HabitusProcessor = new HabitusProcessor(None)
  val files = FileUtils.findFiles(inputDir, ".txt")

  trait Acronym {
    def is(word: String): Boolean
    def mk(word: String): String
  }

  object AllUpperAcronym extends Acronym {

    def is(word: String): Boolean = {
      // This will skip words with non-letters.
      word.forall { letter => letter.isLetter && letter.isUpper }
    }

    def mk(word: String): String = {
      word.toUpperCase
    }
  }

  object OneUpperAcronym extends Acronym {

    def is(word: String): Boolean = {
      word.zipWithIndex.forall { case (char, index) =>
        char.isLetter && ((index == 0 && char.isUpper) || (index != 0 && char.isLower))
      }
    }

    def mk(word: String): String = {
      val letters = word.zipWithIndex.map { case (char, index) =>
        if (!char.isLetter)
          char
        else if (index == 0)
          char.toUpper
        else char.toLower
      }

      "" ++ letters
    }
  }

  def getAcronyms(acronym: Acronym, processor: Processor, text: String): Seq[(String, Int)] = {
    val document = processor.mkDocument(text)
    val wordsAndValids = document.sentences.flatMap { sentence =>
      val valids = sentence.words.scanLeft(false) { case (lowerSeen, word) =>
        lowerSeen || (word.head.isLetter && word.head.isLower)
      }.drop(1)

      sentence.words.zip(valids)
    }
    val validWords = wordsAndValids.filter(_._2).map(_._1)
    val isToWords = validWords.groupBy(acronym.is)
    val trueBag = EqualityHashBag(isToWords(true))
    val falseBag = EqualityHashBag(isToWords(false).map(acronym.mk))
    val countMap = trueBag.map { word =>
      word -> (trueBag.count(word), falseBag.count(word))
    }.toMap
    val goodCountMap = countMap.filter { case (_, (trueCount, falseCount)) =>
      trueCount > 1 && falseCount == 0
    }
    val goodCountSeq = goodCountMap
        .map { case (word, (trueCount, _)) => (word, trueCount) }
        .toSeq
        .sortBy { case (word, trueCount) => (-trueCount, word) }

    goodCountSeq
  }

  files.foreach { file =>
    val text = FileUtils.getTextFromFile(file)
    val allUpperAndCount = getAcronyms(AllUpperAcronym, processor, text)
    val oneUpperAndCount = getAcronyms(OneUpperAcronym, processor, text)

    println(s"\nFile: ${file.getName}\n")
    allUpperAndCount.foreach { case (allUpper, count) =>
      println(s"$allUpper\t$count")
    }
    oneUpperAndCount.foreach { case (allUpper, count) =>
      println(s"$allUpper\t$count")
    }
  }
}
