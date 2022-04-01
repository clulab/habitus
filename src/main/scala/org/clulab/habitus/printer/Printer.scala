package org.clulab.habitus.printer

import org.clulab.odin.Mention
import org.clulab.processors.Document
import org.clulab.utils.FileUtils

import java.io.PrintWriter

abstract class Printer(outputFilename: String) extends Printing {
  protected val printWriter: PrintWriter = FileUtils.printWriterFromFile(outputFilename)
  val na = "N/A"

  protected def outputMention(
    mention: Mention,
    doc: Document,
    inputFilename: String,
    printVariables: PrintVariables
  ): Unit

  def close(): Unit = {
    printWriter.close()
  }

  def outputMentions(
    mentions: Seq[Mention],
    doc: Document,
    inputFilename: String,
    printVariables: PrintVariables
  ): Unit = {
    println(s"Writing mentions from doc $inputFilename to $outputFilename")
    mentions
        .filter { mention => mention.label.matches(printVariables.mentionLabel) }
        .sortBy { mention => (mention.sentence, mention.start) }
        .foreach { mention =>
          outputMention(mention, doc, inputFilename, printVariables)
        }
    printWriter.flush()
  }
}
