package org.clulab.habitus.entitynetworks

import java.io.PrintWriter
import org.clulab.dynet.Utils
import org.clulab.habitus.HabitusProcessor
import org.clulab.utils.FileUtils
import org.clulab.processors.Document

/** Restores the case for words in a provided free text document */
object RestoreCase extends App {
  val inputFileName = args(0)
  val outputFileName = inputFileName + ".restored"
  val pw = new PrintWriter(outputFileName)

  Utils.initializeDyNet()
  val proc = new HabitusProcessor(None)

  val text = FileUtils.getTextFromFile(inputFileName)
  val doc = proc.mkDocumentWithRestoreCase(text)
  saveOutput(pw, doc)
  pw.close()

  private def saveOutput(pw: PrintWriter, doc: Document) {
    for(sent <- doc.sentences) {
      for(i <- sent.indices) {
        if(i > 0) {
          val numberOfSpaces = sent.startOffsets(i) - sent.endOffsets(i - 1)
          for(j <- 0 until numberOfSpaces) {
            pw.print(" ")
          }
        }
        pw.print(sent.words(i))
      }
      pw.println()
    }
  }
}