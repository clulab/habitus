package org.clulab.habitus.apps

import org.clulab.dynet.Utils
import org.clulab.habitus.HabitusProcessor
import org.clulab.habitus.apps.utils.{SentenceUtils, WordTypes}
import org.clulab.processors.{Document, Sentence}
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.FileUtils

import java.io.PrintWriter

/** Compare case when preserved and restored */
object CompareCaseApp extends App {

  class AllGoodHabitusProcessor extends HabitusProcessor(None) {
    // val cutoff = 0.0f // for always restore
    val cutoff = 67.5f

    override def isBadSentence(sentence: Sentence): Boolean = false

    def getPercentNotLower(sentence: Sentence): Float = {

      def countWordType(wordType: WordTypes.WordType): Int =
          sentence.words.count(WordTypes(_) == wordType)

      val wordCount = sentence.words.length
      val allLowerCount = countWordType(WordTypes.AllLower)
      val percentNotLower = (wordCount - allLowerCount).toFloat / wordCount * 100

      percentNotLower
    }

    override def mkDocumentWithRestoreCase(text: String, keepText: Boolean = false): Document = {
      val preservedDocument = mkDocument(text)
      val restoredDocument = super.mkDocumentWithRestoreCase(text)
      val mixedSentences = preservedDocument.sentences.zip(restoredDocument.sentences).map { case (preservedSentence, restoredSentence) =>
        val percentNotLower = getPercentNotLower(preservedSentence)

        if (percentNotLower >= cutoff) restoredSentence
        else preservedSentence
      }

      copyDoc(preservedDocument, mixedSentences)
    }
  }

  val inputFileName = args(0)

  Utils.initializeDyNet()

  val proc = new AllGoodHabitusProcessor()
  val text = FileUtils.getTextFromFile(inputFileName)

  saveOutput(inputFileName + ".preserved", proc.mkDocument(text))
  saveOutput(inputFileName + ".restored", proc.mkDocumentWithRestoreCase(text))

  private def saveOutput(outputFilename: String, doc: Document): Unit = {
    new PrintWriter(outputFilename).autoClose { printWriter =>
      doc.sentences.foreach { sentence =>
        saveOutput(printWriter, sentence)
      }
    }
  }

  private def saveOutput(printWriter: PrintWriter, sentence: Sentence): Unit = {
    val text = SentenceUtils.getText(sentence)

    printWriter.println(text)
  }
}
