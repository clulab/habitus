package org.clulab.habitus.apps

import org.clulab.dynet.Utils
import org.clulab.habitus.HabitusProcessor
import org.clulab.habitus.apps.utils.WordTypes
import org.clulab.processors.{Document, Sentence}
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.FileUtils

import java.io.{File, PrintWriter}

class ConditionalCaseBase {
  val cutoffOpt: Option[Float] = Some(67.5f)
  val removeBad = true

  class ConditionalHabitusProcessor(cutoffOpt: Option[Float] = None, removeBad: Boolean = true) extends HabitusProcessor(None) {

    override def isBadSentence(sentence: Sentence): Boolean =
        if (removeBad) super.isBadSentence(sentence)
        else false

    def getPercentNotLower(sentence: Sentence): Float = {

      def countWordType(wordType: WordTypes.WordType): Int =
          sentence.words.count(WordTypes(_) == wordType)

      val wordCount = sentence.words.length
      val allLowerCount = countWordType(WordTypes.AllLower)
      val percentNotLower = (wordCount - allLowerCount).toFloat / wordCount * 100

      percentNotLower
    }

    override def mkDocumentWithRestoreCase(text: String, keepText: Boolean = false): Document = {
      if (cutoffOpt.isDefined) {
        val cutoff = cutoffOpt.get
        val preservedDocument = mkDocument(text)
        val restoredDocument = super.mkDocumentWithRestoreCase(text)
        val mixedSentences = preservedDocument.sentences.zip(restoredDocument.sentences).map { case (preservedSentence, restoredSentence) =>
          val percentNotLower = getPercentNotLower(preservedSentence)

          if (percentNotLower >= cutoff) restoredSentence
          else preservedSentence
        }

        copyDoc(preservedDocument, mixedSentences)
      }
      else
        // There is no cutoff, so restore everything.
        super.mkDocumentWithRestoreCase(text)
    }
  }

  protected def saveOutput(outputFilename: String, doc: Document): Unit = {
    new PrintWriter(outputFilename).autoClose { printWriter =>
      doc.sentences.foreach { sentence =>
        saveOutput(printWriter, sentence)
      }
    }
  }

  protected def saveOutput(printWriter: PrintWriter, sentence: Sentence): Unit = {
    val text = sentence.words.mkString(" ") // Keep the words tokenized!

    printWriter.println(text)
  }

  def process(files: Seq[File]): Unit = {
    val processor = new ConditionalHabitusProcessor(cutoffOpt, removeBad)

    files.foreach { file =>
      val text = FileUtils.getTextFromFile(file)

      saveOutput(file + ".preserved", processor.mkDocument(text))
      saveOutput(file + ".restored", processor.mkDocumentWithRestoreCase(text))
    }
  }
}
