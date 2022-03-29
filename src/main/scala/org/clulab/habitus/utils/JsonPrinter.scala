package org.clulab.habitus.utils

import org.clulab.odin.Mention
import org.clulab.processors.Document
import org.clulab.serialization.json.stringify
import org.clulab.utils.FileUtils
import org.json4s.JLong
import org.json4s.JsonAST.{JDouble, JField, JInt, JNothing, JObject, JString}
import org.json4s.JsonDSL._

import java.io.PrintWriter

class JsonPrinter(outputFilename: String) extends Printer {
  protected var dirty = false
  protected val printWriter: PrintWriter = FileUtils.printWriterFromFile(outputFilename)
  printWriter.println("[")

  def close(): Unit = {
    if (dirty)
      printWriter.println()
    printWriter.println("]")
    printWriter.close()
  }

  protected def outputMention(
    mention: Mention,
    doc: Document,
    inputFilename: String,
    printVars: PrintVariables
  ): Unit = {
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

    // make an obj from key-value pairs from the context attachment
    val argJObject: JObject = toJObject(mention.attachments.head.asInstanceOf[Context].getArgValuePairs())

    for (value <- argJObject.obj) {
      jObject = jObject ~ (value._1 -> value._2)
    }

    val json = stringify(jObject, pretty = true)
    val indentedJson = "  " + json.replace("\n", "\n  ")

    // Each JSON element in the array needs to be separated from the others.
    if (dirty) printWriter.println(",")
    else dirty = true
    printWriter.print(indentedJson)
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
    val flattenedArgValuePairs = argValuePairs.flatMap { case (arg, value) =>
      value match {
        case None => None
        case Some(x) => Some(arg -> x)
        case x => Some(arg -> x)
      }
    }
    new JObject(flattenedArgValuePairs.map { case (arg, value) => JField(arg,
      value match {
        case f: java.lang.Float => JDouble(f.toDouble)
        case l: java.lang.Long => JLong(l)
        case i: java.lang.Integer => JInt(BigInt(i))
        case s: String => JString(s)
        case x => throw new RuntimeException(s"Cannot serialize something of type ${value.getClass.getName}!")
      }
    )})
  }
}
