package org.clulab.habitus.utils

import org.clulab.odin.Mention
import org.clulab.processors.Document
import org.clulab.utils.FileUtils

import java.io.PrintWriter
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class TsvPrinter(outputFilename: String) {
  protected val printWriter: PrintWriter = FileUtils.printWriterFromFile(outputFilename)

  def close(): Unit = {
    printWriter.close()
  }


  def outputMentions(
                      mentions: Seq[Mention],
                      doc: Document,
                      contexts: mutable.Map[Int, ArrayBuffer[ContextDetails]],
                      inputFilename: String,
                      printVars:PrintVariables
                    ): Unit = {
    println(s"Writing mentions from doc ${inputFilename} to $outputFilename")
    outputMentions(mentions, doc, contexts, inputFilename, printWriter,printVars)
    printWriter.flush()
  }

  // extract needed information and write them to tsv in a desired format. Return nothing here!
  protected def outputMentions(mentions: Seq[Mention], doc: Document, contexts: scala.collection.mutable.Map[Int, ArrayBuffer[ContextDetails]],
                               filename: String, pw: PrintWriter,printVars:PrintVariables): Unit = {
    val mentionsBySentence = mentions groupBy (_.sentence) mapValues (_.sortBy(_.start)) withDefaultValue Nil
    for ((s, i) <- doc.sentences.zipWithIndex) {

      // to keep only mention labelled as Assignment (these labels are associated with .yml files, e.g. Variable, Value)
      val sortedMentions = mentionsBySentence(i).filter(_.label matches printVars.mentionLabel)


      sortedMentions.foreach {
        // Format to print: variable \t value text \t value norms \t extracting sentence \t document name
        // \t Most frequent X within 0 sentences \t Most frequent X within 1 sentences.\t Most frequent X anywhere in the doc.\n
        // Since we only focus on the Assignment mention which includes two submentions in the same format called
        // ``variable`` and ``value`` we access the two through ``arguments`` attribute of the Mention class.
        m =>
          try {

            val varText = m.arguments(printVars.mentionType).head.text
            val value = m.arguments(printVars.mentionExtractor).head
            val valText = value.text
            val sentText = s.getSentenceText
            val valNorms = value.norms

            if (contexts.nonEmpty) {
              val norm =
                if (valNorms.isDefined && valNorms.get.size > 2) {
                  valNorms.filter(_.length > 2).get(0)
                } else {
                  //
                  // not all NEs have meaningful norms set
                  //   For example, DATEs have norms, but CROPs do not
                  // in the latter case, we revert to the lemmas or to the actual text as a backoff
                  //
                  if (value.lemmas.isDefined) {
                    value.lemmas.get.mkString(" ")
                  } else {
                    value.text
                  }
                }

              if (contexts.contains(i)) {


                pw.println(s"$varText\t$valText\t$norm\t$sentText\t$filename\t${
                  contexts(i)(0).mostFreqLoc0Sent
                }\t${
                  contexts(i)(0).mostFreqLoc1Sent
                }\t${
                  contexts(i)(0).mostFreqLoc
                }\t${
                  contexts(i)(0).mostFreqDate0Sent
                }\t${
                  contexts(i)(0).mostFreqDate1Sent
                }\t${
                  contexts(i)(0).mostFreqDate
                }\t${
                  contexts(i)(0).mostFreqCrop0Sent
                }\t${
                  contexts(i)(0).mostFreqCrop1Sent
                }\t${
                  contexts(i)(0).mostFreqCrop
                }\t${
                  contexts(i)(0).mostFreqFertilizer0Sent
                }\t${
                  contexts(i)(0).mostFreqFertilizer1Sent
                }\t${
                  contexts(i)(0).mostFreqFertilizerOverall
                }")


              }
            }
            else
            {
              pw.println(s"$varText\t$valText\t$sentText")
            }
          } catch {
            case e: NoSuchElementException =>
              println(s"No normalized value found for ${m.arguments("value").head.text} in sentence ${s.getSentenceText}!")
              e.printStackTrace()
            case e: RuntimeException =>
              println(s"Error occurs for sentence: ${s.getSentenceText}")
              e.printStackTrace()

          }
      }
    }
  }
}
