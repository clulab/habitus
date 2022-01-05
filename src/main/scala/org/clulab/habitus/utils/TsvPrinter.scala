package org.clulab.habitus.utils

import org.clulab.odin.Mention
import org.clulab.processors.Document
import org.clulab.utils.FileUtils

import java.io.PrintWriter
import scala.collection.mutable

class TsvPrinter(outputFilename: String) extends Printer {
  protected val printWriter: PrintWriter = FileUtils.printWriterFromFile(outputFilename)

  def close(): Unit = {
    printWriter.close()
  }


  def outputMentions(
                      mentions: Seq[Mention],
                      doc: Document,
                      contexts: mutable.Map[Int, ContextDetails],
                      inputFilename: String,
                      printVars:PrintVariables
                    ): Unit = {
    println(s"Writing mentions from doc ${inputFilename} to $outputFilename")
    outputMentions(mentions, doc, contexts, inputFilename, printWriter,printVars)
    printWriter.flush()
  }

  // extract needed information and write them to tsv in a desired format. Return nothing here!
  protected def outputMentions(mentions: Seq[Mention], doc: Document, contexts: mutable.Map[Int, ContextDetails],
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
            val foundBy = m.foundBy

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
                  contexts(i).mostFreqLoc0Sent
                }\t${
                  contexts(i).mostFreqLoc1Sent
                }\t${
                  contexts(i).mostFreqLoc
                }\t${
                  contexts(i).mostFreqDate0Sent
                }\t${
                  contexts(i).mostFreqDate1Sent
                }\t${
                  contexts(i).mostFreqDate
                }\t${
                  contexts(i).mostFreqCrop0Sent
                }\t${
                  contexts(i).mostFreqCrop1Sent
                }\t${
                  contexts(i).mostFreqCrop
                }\t${
                  contexts(i).mostFreqFertilizer0Sent
                }\t${
                  contexts(i).mostFreqFertilizer1Sent
                }\t${
                  contexts(i).mostFreqFertilizerOverall
                }")
              }
            }
            else
            {
              val norm=valNorms.get.head
              //if there are no contexts found, print N/A
              // in some cases (e.g., crop) norm might be empty
              if(norm.isEmpty) {
                pw.println(s"$varText\t$valText\tN/A\t$sentText\t$filename\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A")
              }
              else
                {
                  pw.println(s"$varText\t$valText\t$norm\tN/A\t$sentText\t$filename\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A")
                }
            }
          } catch {
            case e: NoSuchElementException =>
              println(s"Something went wrong while extracting a ${m.label} mention from this sentence: ${m.sentenceObj.getSentenceText}")
              e.printStackTrace()
            case e: RuntimeException =>
              println(s"Error occurs for sentence: ${s.getSentenceText}")
              e.printStackTrace()

          }
      }
    }
  }
}
