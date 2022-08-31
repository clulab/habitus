package org.clulab.habitus.printer

import org.clulab.odin.Mention

trait Printing extends AutoCloseable {

  def outputMentions(mentions: Seq[Mention], inputFilename: String): Unit

  def close(): Unit

  def getArgumentKeys(mention: Mention): Seq[String] =
      mention.arguments.keys.toSeq.filter(mention.arguments(_).nonEmpty).sorted
}
