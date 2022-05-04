package org.clulab.habitus.apps

import org.clulab.dynet.Utils
import org.clulab.habitus.HabitusProcessor
import org.clulab.processors.{Document, Sentence}
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.FileUtils
import org.clulab.wm.eidoscommon.utils.TsvWriter

import java.io.PrintWriter

/** Evaluate the cutoff point for case restoration */
object EvaluateCaseApp extends App {
  val PRESERVED = "preserved"
  val RESTORED = "restored"

  class AllGoodHabitusProcessor extends HabitusProcessor(None) {
    override def isBadSentence(sentence: Sentence): Boolean = false
  }

  object WordTypes extends Enumeration {
    type WordType = Value
    val AllLower, AllUpper, InitialUpper, NonWord, Other = Value
  }

  val inputFileName = args(0)

  Utils.initializeDyNet()

  val proc = new AllGoodHabitusProcessor()
  val text = FileUtils.getTextFromFile(inputFileName)

  saveOutput(inputFileName + ".evaluated.tsv", proc.mkDocument(text), proc.mkDocumentWithRestoreCase(text))

  def saveOutput(outputFilename: String, preservedDoc: Document, restoredDoc: Document): Unit = {
    new PrintWriter(outputFilename).autoClose { pw =>
      val tsvWriter = new TsvWriter(pw)

      tsvWriter.println("Sentence#", "Stage", "WordCount", "NonWordCount", "InitialUpper", "AllUpper", "AllLower", "%AllLower", "Improved", "Text")
      assert(preservedDoc.sentences.length == restoredDoc.sentences.length)
      preservedDoc.sentences.indices.foreach { index =>
        saveOutput(tsvWriter, index, PRESERVED, preservedDoc.sentences(index))
        saveOutput(tsvWriter, index, RESTORED, restoredDoc.sentences(index))
      }
    }
  }

  def getText(sentence: Sentence): String = {
    val separatedWords = sentence.indices.map { i =>
      val separator = {
        val spaceCount =
            if (i == 0) 0
            else sentence.startOffsets(i) - sentence.endOffsets(i - 1)
        " " * spaceCount
      }

      separator + sentence.words(i)
    }

    separatedWords.mkString("")
  }

  def getWordType(word: String): WordTypes.WordType = {
    require(word.nonEmpty)

    val letterCount = word.count(_.isLetter)
    val lowerCount = word.count { char => char.isLetter && char.isLower }
    val upperCount = word.count { char => char.isLetter && char.isUpper }

    if (letterCount == 0)
      WordTypes.NonWord
    else if (lowerCount == letterCount)
      WordTypes.AllLower
    else if (upperCount == letterCount)
      WordTypes.AllUpper
    else if (word.head.isUpper && upperCount == 1)
      WordTypes.InitialUpper
    else WordTypes.Other
  }

  def saveOutput(tsvWriter: TsvWriter, index: Int, stage: String, sentence: Sentence): Unit = {

    def countWordType(wordType: WordTypes.WordType): Int =
      sentence.words.count(getWordType(_) == wordType)

    val wordCount = sentence.words.length
    val nonWordCount = countWordType(WordTypes.NonWord)
    val initialUpperCount = countWordType(WordTypes.InitialUpper)
    val allUpperCount = countWordType(WordTypes.AllUpper)
    val allLowerCount = countWordType(WordTypes.AllLower)
    val percentLower = (allLowerCount.toFloat) / wordCount * 100
    val improved = if (stage == PRESERVED) "-" else "?"
    val text = getText(sentence)

    tsvWriter.println(index.toString, stage, wordCount.toString, nonWordCount.toString, initialUpperCount.toString,
        allUpperCount.toString, allLowerCount.toString, f"$percentLower%.1f", improved, text)
  }
}
