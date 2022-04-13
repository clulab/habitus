package org.clulab.habitus.printer

import org.clulab.habitus.utils.{Lazy, MultiCloser}
import org.clulab.odin.Mention

import scala.collection.mutable

class DynamicArgsTsvPrinter(prefix: String, suffix: String) extends Printing {
  protected val argumentKeysToPrinterMap: mutable.Map[Seq[String], Printer] = new mutable.HashMap()

  def outputMentions(mentions: Seq[Mention], inputFilename: String): Unit = {
    val argumentKeysAndMentions = mentions.groupBy(getArgumentKeys)
    argumentKeysAndMentions.foreach { case (argumentKeys, mentions) =>
      val printer = argumentKeysToPrinterMap.getOrElseUpdate(argumentKeys, {
        val outputFilename = prefix + argumentKeys.mkString("_") + suffix
        new StaticArgsTsvPrinter(outputFilename)
      })
      printer.outputMentions(mentions, inputFilename)
    }
  }

  def close(): Unit = {
    val printers = argumentKeysToPrinterMap.values.map(Lazy(_)).toArray
    val multiCloser = new MultiCloser(printers: _*)

    multiCloser.close()
  }
}
