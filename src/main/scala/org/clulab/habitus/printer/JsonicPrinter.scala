package org.clulab.habitus.printer

import org.clulab.habitus.utils.Context
import org.clulab.odin.{Attachment, Mention}
import org.clulab.processors.Document
import org.json4s.JLong
import org.json4s.JsonAST.{JField, JInt, JObject, JString}
import org.json4s.JsonDSL._

abstract class JsonicPrinter(outputFilename: String) extends Printer(outputFilename) {

  protected def outputMention(
    mention: Mention,
    doc: Document,
    inputFilename: String,
    printVariables: PrintVariables
  ): Unit

  def toJObject(argValuePairs: List[(String, AnyRef)]): JObject = {
    new JObject(argValuePairs.map { case (arg, value) => JField(arg,
        value match {
          case l: java.lang.Long => JLong(l)
          case i: java.lang.Integer => JInt(BigInt(i))
          case s: String => JString(s)
        }
    )})
  }

  def toJObject(attachment: Attachment): JObject =
      toJObject(attachment.asInstanceOf[Context].getArgValuePairs())

  protected def toJObject(mention: Mention, doc: Document, inputFilename: String, printVariables: PrintVariables): JObject = {
    val sentenceText = doc.sentences(mention.sentence).getSentenceText
    val argumentInfo = ArgumentInfo(mention, printVariables)
    val argJObjectOpt = mention.attachments.headOption.map(toJObject)
    var jObject: JObject =
        ("variableText" -> argumentInfo.variableText) ~
        ("valueText" -> argumentInfo.valueText) ~
        ("valueNorm" -> argumentInfo.valueNorm) ~
        ("sentenceText" -> sentenceText) ~
        ("inputFilename" -> inputFilename)

    argJObjectOpt.foreach { argJObject =>
      for (value <- argJObject.obj) {
        jObject = jObject ~ (value._1 -> value._2)
      }
    }
    jObject
  }
}
