package org.clulab.habitus.beliefs

import org.clulab.habitus.utils._
import org.clulab.odin.EventMention
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.{FileUtils, StringUtils, ThreadUtils}

import java.io.{File, PrintWriter}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

// this just means I am extracting potential beliefs on very loose rules
object FuzzyBeliefReader {

  def main(args: Array[String]): Unit = {
    val props = StringUtils.argsToMap(args)
//    val inputDir = "/home/alexeeva/Desktop/habitus_related/data/fuzzyBeliefs/senegal-agriculture-post-processed-science-parse/input"
    val inputDir = "/home/alexeeva/Desktop/habitus_related/data/testingCluPDF2txt/inputForFuzzy/senegal-agriculture-txt-incl-short-paragraphs"
    val outputDir = "/home/alexeeva/Desktop/habitus_related/data/fuzzyBeliefs/senegal-agriculture-post-processed-science-parse/output-short-paragraphs/"
    val threads = 1//props.get("threads").map(_.toInt).getOrElse(1)

    run(inputDir, outputDir, threads)
  }

  def run(inputDir: String, outputDir: String, threads: Int): Unit = {
    new File(outputDir).mkdir()

//    def mkOutputFile(extension: String): String = outputDir + "/mentions" + extension

    val vp = BeliefProcessor()
    val inputFiles = FileUtils.findFiles(inputDir, ".txt")
    val alreadyProcessedNames = FileUtils.findFiles(outputDir, "tsv").map(_.getName.replace(".tsv", ""))
    println("aleady: ", alreadyProcessedNames.mkString("||"))
    val files = inputFiles.filter(file => !alreadyProcessedNames.contains(file.getName.replace(".txt", "")))
    println("not done: ", files.mkString("||"))
    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files




      for (file <- parFiles) {
        val pw = new PrintWriter(outputDir + file.getName.replace(".txt", ".tsv"))
        try {
          val unfiltered = FileUtils.getTextFromFile(file)
          // fixme: temporary, simple text cleanup
          val texts = unfiltered.split("\n\n").map(_.replace("\t", " "))
          val filename = StringUtils.afterLast(file.getName, '/')
          println(s"going to parse input file: $filename")
          for (text <- texts) {
//            print("text: " + text)
            // store ids of done sentences to avoid printing them as context; count per paragraph
            val doneSents = new ArrayBuffer[Int]()
            val cleanText = text.replace("\n", " ").replace("\t", " ")
            val (doc, mentions) = vp.parse(cleanText)
//            for (m <- mentions.filter(_.label matches "Belief")) println("m: " + m.text)
            //          val printVars = PrintVariables("Belief", "believer", "belief")
            //          val context = mutable.Map.empty[Int, ContextDetails]
            // Done: include a check for whether or not there are duplicates in surrounding context - it's fine to have multiple mentions per sent, but no point printing multiple context sentences
            // Done: exclude sentences that are too short (under 10 tokens?) - this can help with sentence tokenizer issues
            // TODO: only doing reported beliefs, right?
            //          multiPrinter.outputMentions(mentions, doc, filename, printVars)
            val beliefMentions = mentions.filter(_.label matches "Belief")
            for (b <- beliefMentions) {

              if (b.sentence > 1) {
                val sentIdx = b.sentence-2
                val prevSent = b.document.sentences(sentIdx)
                if (!doneSents.contains(sentIdx) & prevSent.words.length > 5){
                  val prevSentText = prevSent.getSentenceText
                  pw.println(s"$filename\tcur - 2\t$prevSentText\t\t\t$cleanText\t")
                  doneSents.append(sentIdx)
                }

              }
              if (b.sentence > 0) {
                val sentIdx = b.sentence -1
                val prevSent = b.document.sentences(sentIdx)
                if (!doneSents.contains(sentIdx) & prevSent.words.length > 5) {
                  val prevSentText = prevSent.getSentenceText
                  pw.println(s"$filename\tcur - 1\t$prevSentText\t\t\t$cleanText\t")
                  doneSents.append(sentIdx)
                }
              }
              pw.println(s"$filename\tcurrent\t${b.sentenceObj.getSentenceText}\t${b.asInstanceOf[EventMention].trigger.text}\t${b.text}\t$cleanText\t")
              doneSents.append(b.sentence)
              if (b.sentence + 1 < b.document.sentences.length) {
                val sentIdx = b.sentence + 1
                val nextSent = b.document.sentences(sentIdx)
                if (!doneSents.contains(sentIdx) & nextSent.words.length > 5) {
                  val nextSentText = nextSent.getSentenceText
                  pw.println(s"$filename\tcur + 1\t$nextSentText\t\t\t$cleanText\t")
                  doneSents.append(sentIdx)
                }

              }
              if (b.sentence + 2 < b.document.sentences.length) {
                val sentIdx = b.sentence + 2
                val nextSent = b.document.sentences(sentIdx)
                if (!doneSents.contains(sentIdx) & nextSent.words.length > 5) {
                  val nextSentText = nextSent.getSentenceText
                  pw.println(s"$filename\tcur + 2\t$nextSentText\t\t\t$cleanText\t")
                  doneSents.append(sentIdx)
                }

              }

            }

          }
        }
        catch {
          case e: Exception => e.printStackTrace()
        }
        pw.close()
      }

    }

}