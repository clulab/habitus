package org.clulab.habitus.utils

import org.clulab.odin.Mention
import org.clulab.processors.Document
import org.clulab.serialization.json.stringify
import org.clulab.utils.FileUtils
import org.json4s.JsonDSL._

import java.io.PrintWriter
import scala.collection.mutable

class JsonlPrinter(outputFilename: String) extends Printer {
  protected val printWriter: PrintWriter = FileUtils.printWriterFromFile(outputFilename)

  def close(): Unit = {
    printWriter.close()
  }

  protected def outputMention(
    mention: Mention,
    doc: Document,
    contextDetailsMap: mutable.Map[Int, ContextDetails],
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
    val jObject = contextDetailsMap
        .get(mention.sentence) // Get it, optionally, if it is there.
        .map { contextDetails => // If there, do this.
          ("variableText" -> variableText) ~
          ("valueText" -> valueText) ~
          ("valueNorm" -> valueNorm) ~
          ("sentenceText" -> sentenceText) ~
          ("inputFilename" -> inputFilename) ~
          ("mostFreqLoc0Sent" -> contextDetails.mostFreqLoc0Sent) ~
          ("mostFreqLoc1Sent" -> contextDetails.mostFreqLoc1Sent) ~
          ("mostFreqLoc" -> contextDetails.mostFreqLoc) ~
          ("mostFreqDate0Sent" -> contextDetails.mostFreqDate0Sent) ~
          ("mostFreqDate1Sent" -> contextDetails.mostFreqDate1Sent) ~
          ("mostFreqDate" -> contextDetails.mostFreqDate) ~
          ("mostFreqCrop0Sent" -> contextDetails.mostFreqCrop0Sent) ~
          ("mostFreqCrop1Sent" -> contextDetails.mostFreqCrop1Sent) ~
          ("mostFreqCrop" -> contextDetails.mostFreqCrop) ~
          ("mostFreqFert0Sent" -> contextDetails.mostFreqFertilizer0Sent) ~
          ("mostFreqFert1Sent" -> contextDetails.mostFreqFertilizer1Sent) ~
          ("mostFreqFert" -> contextDetails.mostFreqFertilizerOverall)
        }
        .getOrElse { // If it wasn't there, do this instead.
          // These keys should match the ones used above.
          ("variableText" -> variableText) ~
          ("valueText" -> valueText) ~
          ("sentenceText" -> sentenceText)
        }
    val json = stringify(jObject, pretty = false)
    val jsonl = json.replace('\n', ' ') // just in case

    printWriter.println(jsonl)
  }

  def outputMentions(
    mentions: Seq[Mention],
    doc: Document,
    contextDetailsMap: mutable.Map[Int, ContextDetails],
    inputFilename: String,
    printVars: PrintVariables
  ): Unit = {
    println(s"Writing mentions from doc $inputFilename to $outputFilename")
    mentions
        .filter { mention => mention.label.matches(printVars.mentionLabel) }
        .filter { mention => contextDetailsMap.isEmpty || contextDetailsMap.contains(mention.sentence) }
        .sortBy { mention => (mention.sentence, mention.start) }
        .foreach { mention =>
          outputMention(mention, doc, contextDetailsMap, inputFilename, printVars)
        }
    printWriter.flush()
  }
}