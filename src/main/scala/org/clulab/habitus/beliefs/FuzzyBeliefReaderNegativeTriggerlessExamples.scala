package org.clulab.habitus.beliefs

import org.clulab.odin.EventMention
import org.clulab.processors.Sentence
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.fastnlp.FastNLPProcessor
import org.clulab.utils.{FileUtils, StringUtils, ThreadUtils}

import java.io.{File, PrintWriter}
import java.util.concurrent.ThreadLocalRandom
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

// this just means I am extracting potential beliefs on very loose rules
object FuzzyBeliefReaderNegativeTriggerlessExamples {

  val triggerWords = Seq("believe","think","feel","trust","doubt","fear","skeptical","pessimistic","optimistic","hopeful","worry","sure","unsure","convinced","confident","positive","hesitant","suppose","suspect","perceive","predict","credible","convincing","believable","conceivable","inconceivable","plausible","questionable","dubious","conclusive","hard","likely","believe","feel","think","expect","difficult","unlikely", "describe","say","guess","worry","suspect","perceive","expect","want","prefer","hope","overestimate","underestimate","stigma","willingness","popular","consider","find","value","describe","recognize","like","eager","regard","appreciate","traditionally","customarily","optimistic","pessimistic","hopeful","sure","unsure","convinced","confident","positive","hesitant","emphasis").distinct
  def main(args: Array[String]): Unit = {
    val props = StringUtils.argsToMap(args)
//    val inputDir = "/home/alexeeva/Desktop/habitus_related/data/fuzzyBeliefs/senegal-agriculture-post-processed-science-parse/input"
//    val inputDir = "/home/alexeeva/Desktop/habitus_related/data/testingCluPDF2txt/inputForFuzzy/senegal-agriculture-txt-incl-short-paragraphs"
//    val inputDir = "/home/alexeeva/Desktop/habitus_related/data/query_results/habitus_rice_growing_senegal/txt"
//    val inputDir = "/home/alexeeva/Desktop/habitus_related/data/query_results/habitus_rice_trade_crops_actors_senegal/txt"
//    val inputDir = "/home/alexeeva/Desktop/habitus_related/data/Rice_growing_in_Senegal_River_Valley/pdf2txt-clean-science-parse"
//    val inputDir = "/home/alexeeva/Desktop/habitus_related/data/fuzzyBeliefs/uganda_vs_5_others/regular-science-parse-txt/science-parsed-txt"
val inputDir = "/home/alexeeva/Desktop/habitus_related/data/fuzzyBeliefs/Rice_growing_in_Senegal_River_Valley/pdf2txt-clean-science-parse"
//    val outputDir = "/home/alexeeva/Desktop/habitus_related/data/fuzzyBeliefs/senegal-agriculture-post-processed-science-parse/output-short-paragraphs/"
//    val outputDir = "/home/alexeeva/Desktop/habitus_related/data/query_results/habitus_rice_growing_senegal/fuzzy-beliefs/"
//    val outputDir = "/home/alexeeva/Desktop/habitus_related/data/query_results/habitus_rice_trade_crops_actors_senegal/fuzzy-beliefs"
//    val outputDir = "/home/alexeeva/Desktop/habitus_related/data/fuzzyBeliefs/Rice_growing_in_Senegal_River_Valley/fuzzy_mentions_jun13/"
//    val outputDir = "/home/alexeeva/Desktop/habitus_related/data/fuzzyBeliefs/uganda_vs_5_others/triggerless-negative-examples/"
    val outputDir = "/home/alexeeva/Desktop/habitus_related/data/fuzzyBeliefs/Rice_growing_in_Senegal_River_Valley/triggerless-negative-examples/"
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
    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files




      for (file <- parFiles) {
//        val allTriggerlessSentences = new ArrayBuffer[(Sentence, String)]()

        val outFileName = outputDir + file.getName.replace(".txt", ".tsv")
//        print("ofn: " + outFileName)
        val pw = new PrintWriter(outFileName)
//        print(pw + "<<")
        try {
          val unfiltered = FileUtils.getTextFromFile(file)
          def containsVocab(text: String, exclVocab: Seq[String]): Boolean = {
            for (ev <- exclVocab) {
              if (text.contains(ev)) return true
            }
            false
          }
          val excludeVocab = Seq(" rape", "genital")
          // fixme: temporary, simple text cleanup
          val texts = unfiltered.split("\n\n").map(_.replace("\t", " ")).filter(_.length < 2300).filterNot(t => containsVocab(t, excludeVocab))
          val filename = StringUtils.afterLast(file.getName, '/')
          println(s"going to parse input file: $filename")
          for (text <- texts) {
//            print("text: " + text)
            // store ids of done sentences to avoid printing them as context; count per paragraph
            val doneSents = new ArrayBuffer[Int]()
            val cleanText = text.replace("\n", " ").replace("\t", " ")
            // we just need lemmas so clu processor (not habitus processor) will do
            val processor = new FastNLPProcessor()
            val doc = processor.annotate(cleanText)
            println("doc len: " + doc.sentences.length)




            for (sent <- doc.sentences) {
//              if (Random.between())
              if (ThreadLocalRandom.current().nextInt(0, 100) < 5) {
                if (sent.words.length > 5 && sent.lemmas.get.intersect(triggerWords).isEmpty) {
//                  allTriggerlessSentences.append((sent, cleanText))
//                  println("sent: " + sent.words.slice(0, 5).mkString(" ") + " " + allTriggerlessSentences.length)
                  pw.println(s"$filename\tcurrent\t${sent.getSentenceText}\tN/A\tN/A\t$cleanText\t")
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