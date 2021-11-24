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
}
