package org.clulab.habitus.printer

import org.clulab.habitus.utils.{Context, PrintVariables}
import org.clulab.odin.Mention
import org.clulab.processors.Document
import org.clulab.utils.FileUtils
import org.json4s.JLong
import org.json4s.JsonAST.{JField, JInt, JObject, JString}
import org.json4s.JsonDSL._

import java.io.PrintWriter

abstract class JsonicPrinter(outputFilename: String) extends Printer {
  protected val printWriter: PrintWriter = FileUtils.printWriterFromFile(outputFilename)

  protected def outputMention(
    mention: Mention,
    doc: Document,
    inputFilename: String,
    printVars: PrintVariables
  ): Unit

  def close(): Unit = {
    printWriter.close()
  }

  protected def toJObject(mention: Mention, doc: Document, inputFilename: String, printVars: PrintVariables): JObject = {
    val variableText = mention.arguments(printVars.mentionType).head.text
    val valueMention = mention.arguments(printVars.mentionExtractor).head
    val valueText = valueMention.text
    val sentenceText = doc.sentences(mention.sentence).getSentenceText
    val valueNormsOpt = valueMention.norms
    val valueNorm =
      if (valueNormsOpt.isDefined && valueNormsOpt.get.length > 2)
        valueNormsOpt.get.head
      else
        valueMention.lemmas
          .map(_.mkString(" "))
          .getOrElse(valueMention.text)

    var jObject: JObject =
        ("variableText" -> variableText) ~
        ("valueText" -> valueText) ~
        ("valueNorm" -> valueNorm) ~
        ("sentenceText" -> sentenceText) ~
        ("inputFilename" -> inputFilename)

    val context = mention.attachments.headOption
    val argJObject: Option[JObject] = if (context.isDefined) Some(toJObject(context.get.asInstanceOf[Context].getArgValuePairs())) else None

    if (argJObject.isDefined) {
      for (value <- argJObject.get.obj) {
        jObject = jObject ~ (value._1 -> value._2)
      }
    }
    jObject
  }

  def outputMentions(
    mentions: Seq[Mention],
    doc: Document,
    inputFilename: String,
    printVars: PrintVariables
  ): Unit = {
    println(s"Writing mentions from doc $inputFilename to $outputFilename")
    mentions
        .filter { mention => mention.label.matches(printVars.mentionLabel) }
        .sortBy { mention => (mention.sentence, mention.start) }
        .foreach { mention =>
          outputMention(mention, doc, inputFilename, printVars)
        }
    printWriter.flush()
  }

  def toJObject(argValuePairs: List[(String, AnyRef)]): JObject = {
    new JObject(argValuePairs.map { case (arg, value) => JField(arg,
        value match {
          case l: java.lang.Long => JLong(l)
          case i: java.lang.Integer => JInt(BigInt(i))
          case s: String => JString(s)
        }
    )})
  }
}
