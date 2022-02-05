package org.clulab.habitus.utils

import org.clulab.odin.Mention
import org.clulab.processors.Document
import org.clulab.serialization.json.stringify
import org.clulab.utils.FileUtils
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._

import java.io.PrintWriter

class JsonlPrinter(outputFilename: String) extends Printer {
  protected val printWriter: PrintWriter = FileUtils.printWriterFromFile(outputFilename)

  def close(): Unit = {
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

    // add key-value pairs from the context attachment
    mention.attachments.head.asInstanceOf[Context].getArgs()
      .foreach {
        i =>
          jObject = jObject ~ (i._1 -> i._2.toString)
      }

    val json = stringify(jObject, pretty = false)
    val jsonl = json.replace('\n', ' ') // just in case

    printWriter.println(jsonl)
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
//        .filter { mention => contextDetailsMap.isEmpty || contextDetailsMap.contains(mention.sentence) }
        .sortBy { mention => (mention.sentence, mention.start) }
        .foreach { mention =>
          outputMention(mention, doc, inputFilename, printVars)
        }
    printWriter.flush()
  }
}
