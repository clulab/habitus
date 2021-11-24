package org.clulab.habitus.utils

import org.clulab.odin.Mention
import org.clulab.processors.Document

import scala.collection.mutable

trait Printer {
  def outputMentions(
    mentions: Seq[Mention],
    doc: Document,
    contextDetailsMap: mutable.Map[Int, ContextDetails],
    inputFilename: String,
    printVars: PrintVariables
  ): Unit

  def close(): Unit
}

class MultiPrinter(values: MultiCloser.Constructor[Printer]*) extends MultiCloser[Printer](values: _*) with Printer {
  val printers = constructeds

  def outputMentions(
    mentions: Seq[Mention],
    doc: Document,
    contextDetailsMap: mutable.Map[Int, ContextDetails],
    inputFilename: String,
    printVars: PrintVariables
  ): Unit = synchronized {
    // Prevent not only overlapping output within a printer,
    // but ensure each printer has the same order of output.
    printers.foreach { printer =>
      printer.outputMentions(mentions, doc, contextDetailsMap, inputFilename, printVars)
    }
  }
}
