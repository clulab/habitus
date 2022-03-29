package org.clulab.habitus.printer

import org.clulab.habitus.utils.PrintVariables
import org.clulab.odin.Mention
import org.clulab.processors.Document
import org.clulab.serialization.json.stringify

class JsonPrinter(outputFilename: String) extends JsonicPrinter(outputFilename) {
  protected var dirty = false
  printWriter.println("[")

  override def close(): Unit = {
    if (dirty)
      printWriter.println()
    printWriter.println("]")
    super.close()
  }

  protected def outputMention(
    mention: Mention,
    doc: Document,
    inputFilename: String,
    printVars: PrintVariables
  ): Unit = {

    val jObject = toJObject(mention, doc, inputFilename, printVars)
    val json = stringify(jObject, pretty = true)
    val indentedJson = "  " + json.replace("\n", "\n  ")

    // Each JSON element in the array needs to be separated from the others.
    if (dirty) printWriter.println(",")
    else dirty = true
    printWriter.print(indentedJson)
  }
}
