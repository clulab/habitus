package org.clulab.habitus.printer

import org.clulab.habitus.utils.PrintVariables
import org.clulab.odin.Mention
import org.clulab.processors.Document

trait Printer {
  val na = "N/A"

  def outputMentions(
    mentions: Seq[Mention],
    doc: Document,
    inputFilename: String,
    printVars: PrintVariables
  ): Unit

  def close(): Unit
}
