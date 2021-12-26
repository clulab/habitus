package org.clulab.habitus.utils

import org.clulab.odin.Mention
import org.clulab.processors.Document
import org.clulab.serialization.json.stringify
import org.clulab.utils.FileUtils
import org.json4s.JsonDSL._

import java.io.PrintWriter
import scala.collection.mutable

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
            ("valueNorm" -> valueNorm) ~           
            ("sentenceText" -> sentenceText) ~
            ("inputFilename" -> inputFilename) ~
            ("mostFreqLoc0Sent" -> "N/A") ~
            ("mostFreqLoc1Sent" -> "N/A") ~
            ("mostFreqLoc" -> "N/A") ~
            ("mostFreqDate0Sent" -> "N/A") ~
            ("mostFreqDate1Sent" -> "N/A") ~
            ("mostFreqDate" -> "N/A") ~
            ("mostFreqCrop0Sent" -> "N/A") ~
            ("mostFreqCrop1Sent" -> "N/A") ~
            ("mostFreqCrop" -> "N/A") ~
            ("mostFreqFert0Sent" -> "N/A") ~
            ("mostFreqFert1Sent" -> "N/A") ~
            ("mostFreqFert" -> "N/A")
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
