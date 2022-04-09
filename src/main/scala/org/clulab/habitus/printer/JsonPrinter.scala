package org.clulab.habitus.printer

import org.clulab.habitus.utils.Context
import org.clulab.serialization.json.stringify

import java.io.File

class JsonPrinter(outputFile: File) extends JsonicPrinter(outputFile) {

  def this(outputFilename: String) = this(new File(outputFilename))

  protected var dirty = false
  printWriter.println("[")

  override def close(): Unit = {
    if (dirty)
      printWriter.println()
    printWriter.println("]")
    super.close()
  }

  def outputInfos(
    mentionInfo: MentionInfo,
    contextInfo: Context,
    argumentInfos: Seq[ArgumentInfo]
  ): Unit = {
    val jObject = toJObject(mentionInfo, contextInfo, argumentInfos)
    val json = stringify(jObject, pretty = true).replace("\r\n", "\n")
    val indentedJson = "  " + json.replace("\n", "\n  ")

    // Each JSON element in the array needs to be separated from the others.
    if (dirty) printWriter.println(",")
    else dirty = true
    printWriter.print(indentedJson)
  }
}
