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
    inputFilename: String
  ): Unit = {
    println(s"Writing mentions from doc ${inputFilename} to $outputFilename")
    outputMentions(mentions, doc, contexts, inputFilename, printWriter);
    printWriter.flush()
  }

  // extract needed information and write them to tsv in a desired format. Return nothing here!
  protected def outputMentions(mentions: Seq[Mention], doc: Document, contexts: scala.collection.mutable.Map[Int, ArrayBuffer[ContextDetails]],
                          filename: String, pw: PrintWriter): Unit = {
    val mentionsBySentence = mentions groupBy (_.sentence) mapValues (_.sortBy(_.start)) withDefaultValue Nil
    for ((s, i) <- doc.sentences.zipWithIndex) {

      // to keep only mention labelled as Assignment (these labels are associated with .yml files, e.g. Variable, Value)
      val sortedMentions = mentionsBySentence(i).filter(_.label matches "Assignment")


      sortedMentions.foreach {
        // Format to print: variable \t value text \t value norms \t extracting sentence \t document name
        // \t Most frequent LOC within 0 sentences \t Most frequent LOC within 1 sentences.\t Most frequent LOC anywhere in the doc.\n
        // Since we only focus on the Assignment mention which includes two submentions in the same format called
        // ``variable`` and ``value`` we access the two through ``arguments`` attribute of the Mention class.
        m =>
          try {

            val varText = m.arguments("variable").head.text
            val value = m.arguments("value").head
            val valText = value.text
            val sentText = s.getSentenceText
            val valNorms = value.norms

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
              for (context <- contexts(i)) {
                pw.println(s"$varText\t$valText\t$norm\t$sentText\t$filename\t${
                  context.mostFreqLoc0Sent
                }\t${
                  context.mostFreqLoc1Sent
                }\t${
                  context.mostFreqLoc
                }\t${
                  context.mostFreqDate0Sent
                }\t${
                  context.mostFreqDate1Sent
                }\t${
                  context.mostFreqDate
                }\t${
                  context.mostFreqCrop0Sent
                }\t${
                  context.mostFreqCrop1Sent
                }\t${
                  context.mostFreqCrop
                }")
              }
            }
            else {

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




  def outputBeliefMentions(
                      mentions: Seq[Mention],
                      doc: Document,
                      contexts: mutable.Map[Int, ArrayBuffer[ContextDetails]],
                      inputFilename: String
                    ): Unit = {
    println(s"Writing mentions from doc ${inputFilename} to $outputFilename")
    outputBeliefMentions(mentions, doc, contexts, inputFilename, printWriter);
    printWriter.flush()
  }

  // extract needed information and write them to tsv in a desired format. Return nothing here!
  protected def outputBeliefMentions(mentions: Seq[Mention], doc: Document, contexts: scala.collection.mutable.Map[Int, ArrayBuffer[ContextDetails]],
                               filename: String, pw: PrintWriter): Unit = {
    val mentionsBySentence = mentions groupBy (_.sentence) mapValues (_.sortBy(_.start)) withDefaultValue Nil
    for ((s, i) <- doc.sentences.zipWithIndex) {

      // to keep only mention labelled as Assignment (these labels are associated with .yml files, e.g. Variable, Value)
      val sortedMentions = mentionsBySentence(i).filter(_.label matches "Belief")


      sortedMentions.foreach {
        // Format to print: variable \t value text \t value norms \t extracting sentence \t document name
        // \t Most frequent LOC within 0 sentences \t Most frequent LOC within 1 sentences.\t Most frequent LOC anywhere in the doc.\n
        // Since we only focus on the Assignment mention which includes two submentions in the same format called
        // ``variable`` and ``value`` we access the two through ``arguments`` attribute of the Mention class.
        m =>
          try {
            val believer = m.arguments("believer").head.text
            val belief = m.arguments("belief").head.text
            pw.println(s"$believer\t$belief")
          }
          catch {
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
