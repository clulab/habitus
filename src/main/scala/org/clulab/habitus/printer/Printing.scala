package org.clulab.habitus.printer

import org.clulab.odin.Mention
import org.clulab.processors.Document

trait Printing {

  def outputMentions(
    mentions: Seq[Mention],
    inputFilename: String
  ): Unit

  def close(): Unit
}
