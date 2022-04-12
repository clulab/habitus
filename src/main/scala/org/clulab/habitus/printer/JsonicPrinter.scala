package org.clulab.habitus.printer

import org.clulab.habitus.utils.Context
import org.json4s.{DefaultFormats, Extraction, Formats}
import org.json4s.JsonAST.{JArray, JObject}
import org.json4s.JsonDSL._

import java.io.File

abstract class JsonicPrinter(outputFile: File) extends Printer(outputFile) {
  implicit val formats: Formats = DefaultFormats

  protected def toJObject(
    mentionInfo: MentionInfo,
    contextInfo: Context,
    argumentInfos: Seq[ArgumentInfo]
  ): JObject = {
    val mention = Extraction.decompose(mentionInfo).asInstanceOf[JObject]
    val context = Extraction.decompose(contextInfo).asInstanceOf[JObject]
    val arguments = JArray(argumentInfos.toList.map { argumentInfo =>
      Extraction.decompose(argumentInfo)
    })

    mention ~
        ("context" -> context) ~
        ("arguments" -> arguments)
  }
}
