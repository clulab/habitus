package org.clulab.habitus.printer

import org.clulab.habitus.utils.PrintVariables
import org.clulab.odin.Mention
import org.clulab.processors.Document
import org.clulab.serialization.json.stringify

class JsonlPrinter(outputFilename: String) extends JsonicPrinter(outputFilename) {

  protected def outputMention(
    mention: Mention,
    doc: Document,
    inputFilename: String,
    printVars: PrintVariables
  ): Unit = {
    val jObject = toJObject(mention, doc, inputFilename, printVars)
    val json = stringify(jObject, pretty = false)
    val jsonl = json.replace('\n', ' ') // just in case

    printWriter.println(jsonl)
  }
}
