package org.clulab.habitus.printer

import org.clulab.habitus.utils.Context
import org.clulab.serialization.json.stringify

import java.io.File

class JsonlPrinter(outputFile: File) extends JsonicPrinter(outputFile) {

  def this(outputFilename: String) = this (new File(outputFilename))

  def outputInfos(
    mentionInfo: MentionInfo,
    contextInfo: Context,
    argumentInfos: Seq[ArgumentInfo]
  ): Unit = {
    val jObject = toJObject(mentionInfo, contextInfo, argumentInfos)
    val json = stringify(jObject, pretty = false)
    val jsonl = json.replace('\n', ' ') // just in case

    printWriter.println(jsonl)
  }
}
