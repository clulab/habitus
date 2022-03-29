package org.clulab.habitus.printer

import org.clulab.habitus.utils.{Lazy, MultiCloser}
import org.clulab.odin.Mention
import org.clulab.processors.Document

class MultiPrinter(lazies: Lazy[Printer]*) extends MultiCloser[Printer](lazies: _*) with Printer {
  val printers: Array[Printer] = values

  def outputMentions(
    mentions: Seq[Mention],
    doc: Document,
    inputFilename: String,
    printVars: PrintVariables
  ): Unit = synchronized {
    // Prevent not only overlapping output within a printer,
    // but ensure each printer has the same order of output.
    printers.foreach { printer =>
      printer.outputMentions(mentions, doc, inputFilename, printVars)
    }
  }
}
