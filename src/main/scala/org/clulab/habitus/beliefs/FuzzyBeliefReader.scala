package org.clulab.habitus.beliefs

import org.clulab.habitus.utils._
import org.clulab.odin.EventMention
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.{FileUtils, StringUtils, ThreadUtils}

import java.io.{File, PrintWriter}
import scala.collection.mutable

// this just means I am extracting potential beliefs on very loose rules
object FuzzyBeliefReader {

  def main(args: Array[String]): Unit = {
    val props = StringUtils.argsToMap(args)
    val inputDir = "/home/alexeeva/Desktop/habitus_related/data/fuzzyBeliefs/processed_input"
    val outputDir = "/home/alexeeva/Desktop/habitus_related/data/fuzzyBeliefs/test_output"
    val threads = 1//props.get("threads").map(_.toInt).getOrElse(1)

    run(inputDir, outputDir, threads)
  }

  def run(inputDir: String, outputDir: String, threads: Int): Unit = {
    new File(outputDir).mkdir()

    def mkOutputFile(extension: String): String = outputDir + "/mentions" + extension

    val vp = BeliefProcessor()
    val files = FileUtils.findFiles(inputDir, ".txt")
    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files


    val pw = new PrintWriter(mkOutputFile(".tsv"))

      for (file <- parFiles) {
        try {
          val unfiltered = FileUtils.getTextFromFile(file)
          // fixme: temporary, simple text cleanup
          val texts = unfiltered.split("\n\n")//.replace("\n",
//            " ").replace("- ", "")
          val filename = StringUtils.afterLast(file.getName, '/')
          println(s"going to parse input file: $filename")
          for (text <- texts) {
            val cleanText = text.replace("\n", " ").replace("\t", " ")
            val (doc, mentions) = vp.parse(cleanText)
            for (m <- mentions.filter(_.label matches "Belief")) println("m: " + m.text)
            //          val printVars = PrintVariables("Belief", "believer", "belief")
            //          val context = mutable.Map.empty[Int, ContextDetails]
            // TODO: include a check for whether or not there are duplicates in surrounding context - it's fine to have multiple mentions per sent, but no point printing multiple context sentences
            // TODO: exclude sentences that are too short (under 10 tokens?) - this can help with sentence tokenizer issues
            // TODO: only doing reported beliefs, right?
            //          multiPrinter.outputMentions(mentions, doc, filename, printVars)
            val beliefMentions = mentions.filter(_.label matches "Belief")
            for (b <- beliefMentions) {

              if (b.sentence > 1) {
                val prevSent = b.document.sentences(b.sentence-2).getSentenceText
                pw.println(s"cur - 2\t$prevSent\t\t\t$cleanText\t")
              }
              if (b.sentence > 0) {
                val prevSent = b.document.sentences(b.sentence-1).getSentenceText
                pw.println(s"cur - 1\t$prevSent\t\t\t$cleanText\t")
              }
              pw.println(s"current\t${b.sentenceObj.getSentenceText}\t${b.asInstanceOf[EventMention].trigger.text}\t${b.text}\t$cleanText\t")
              if (b.sentence + 1 < b.document.sentences.length) {
                val nextSent = b.document.sentences(b.sentence + 1).getSentenceText
                pw.println(s"cur + 1\t$nextSent\t\t\t$cleanText\t")

              }
              if (b.sentence + 2 < b.document.sentences.length) {
                val nextSent = b.document.sentences(b.sentence + 2).getSentenceText
                pw.println(s"cur + 2\t$nextSent\t\t\t$cleanText\t")

              }

            }

          }
        }
        catch {
          case e: Exception => e.printStackTrace()
        }
      }
    pw.close()
    }

}