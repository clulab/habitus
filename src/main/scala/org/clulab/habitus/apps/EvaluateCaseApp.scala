package org.clulab.habitus.apps

import org.clulab.dynet.Utils
import org.clulab.habitus.HabitusProcessor
import org.clulab.habitus.apps.utils.{SentenceUtils, WordTypes}
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

  val inputFileName = args(0)

  val proc = new AllGoodHabitusProcessor()
  val text = FileUtils.getTextFromFile(inputFileName)

  saveOutput(inputFileName + ".evaluated.tsv", proc.mkDocument(text), proc.mkDocumentWithRestoreCase(text))

  def saveOutput(outputFilename: String, preservedDoc: Document, restoredDoc: Document): Unit = {
    new PrintWriter(outputFilename).autoClose { printWriter =>
      val tsvWriter = new TsvWriter(printWriter)

      tsvWriter.println("Sentence#", "Stage", "WordCount", "NonWordCount", "InitialUpper", "AllUpper", "AllLower", "%NotAllLower", "Improved", "Text")
      assert(preservedDoc.sentences.length == restoredDoc.sentences.length)
      preservedDoc.sentences.indices.foreach { index =>
        saveOutput(tsvWriter, index, PRESERVED, preservedDoc.sentences(index))
        saveOutput(tsvWriter, index, RESTORED, restoredDoc.sentences(index))
      }
    }
  }

  def saveOutput(tsvWriter: TsvWriter, index: Int, stage: String, sentence: Sentence): Unit = {

    def countWordType(wordType: WordTypes.WordType): Int =
        sentence.words.count(WordTypes(_) == wordType)

    val wordCount = sentence.words.length
    val nonWordCount = countWordType(WordTypes.NonWord)
    val initialUpperCount = countWordType(WordTypes.InitialUpper)
    val allUpperCount = countWordType(WordTypes.AllUpper)
    val allLowerCount = countWordType(WordTypes.AllLower)
    val percentNotLower = (wordCount - allLowerCount).toFloat / wordCount * 100
    val improved = if (stage == PRESERVED) "?" else "-"
    val text = SentenceUtils.getText(sentence)

    tsvWriter.println(index.toString, stage, wordCount.toString, nonWordCount.toString, initialUpperCount.toString,
        allUpperCount.toString, allLowerCount.toString, f"$percentNotLower%.1f%%", improved, text)
  }
}
