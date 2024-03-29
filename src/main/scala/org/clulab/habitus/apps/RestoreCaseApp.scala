package org.clulab.habitus.apps

import org.clulab.dynet.Utils
import org.clulab.habitus.HabitusProcessor
import org.clulab.habitus.apps.utils.SentenceUtils
import org.clulab.processors.{Document, Sentence}
import org.clulab.utils.FileUtils

import java.io.PrintWriter
import scala.util.Using

/** Restores the case for words in a provided free text document */
object RestoreCaseApp extends App {
  val inputFileName = args(0)

  val proc = new HabitusProcessor(None)
  val text = FileUtils.getTextFromFile(inputFileName)

  saveOutput(inputFileName + ".preserved", proc.mkDocument(text))
  saveOutput(inputFileName + ".restored", proc.mkDocumentWithRestoreCase(text))

  private def saveOutput(outputFilename: String, doc: Document): Unit = {
    Using.resource(new PrintWriter(outputFilename)) { pw =>
      doc.sentences.foreach { sentence =>
        saveOutput(pw, sentence)
      }
    }
  }

  private def saveOutput(pw: PrintWriter, sentence: Sentence): Unit = {
    val text = SentenceUtils.getText(sentence)

    pw.println(text)
    pw.println()
  }
}
