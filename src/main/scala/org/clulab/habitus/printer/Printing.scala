package org.clulab.habitus.printer

import org.clulab.odin.Mention
import org.clulab.processors.Document

trait Printing {

  def outputMentions(
    mentions: Seq[Mention],
    doc: Document,
    inputFilename: String,
    printVars: PrintVariables
  ): Unit

  def close(): Unit
}
