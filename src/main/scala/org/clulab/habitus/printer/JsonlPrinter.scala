package org.clulab.habitus.printer

import org.clulab.odin.Mention
import org.clulab.processors.Document
import org.clulab.serialization.json.stringify

class JsonlPrinter(outputFilename: String) extends JsonicPrinter(outputFilename) {

  protected def outputMention(
    mention: Mention,
    doc: Document,
    inputFilename: String,
    printVariables: PrintVariables
  ): Unit = {
    val jObject = toJObject(mention, doc, inputFilename, printVariables)
    val json = stringify(jObject, pretty = false)
    val jsonl = json.replace('\n', ' ') // just in case

    printWriter.println(jsonl)
  }
}
