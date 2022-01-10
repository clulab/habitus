package org.clulab.habitus.interviews

import org.clulab.utils.{FileUtils, StringUtils, ThreadUtils}
import org.clulab.wm.eidos.SimpleEidos
import org.clulab.wm.eidos.serialization.simple.SimpleSerializer

import java.io.{File, PrintWriter}

object InterviewsReader {

  def main(args: Array[String]): Unit = {
    val inputDir = ""
    val outputDir = ""
    val threads = 1

    example()
    run(inputDir, outputDir, threads)
  }

  def example(): Unit = {
    val text = "This is a test.  Water trucking in Ethiopia has decreased over August due to the cost of fuel."
    val eidosSystem = SimpleEidos(useGeoNorm = false, useTimeNorm = false)
    val annotatedDocument = eidosSystem.extractFromText(text)
    val simpleSerializer = SimpleSerializer(annotatedDocument)

    simpleSerializer.serialize()
  }

  def run(inputDir: String, outputDir: String, threads: Int): Unit = {
    new File(outputDir).mkdir()

    val vp = InterviewsProcessor()
    val files = FileUtils.findFiles(inputDir, ".txt")
    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files
    val pw = new PrintWriter(new File(outputDir + "/mentions.tsv"))
    pw.write("filename\tmention type\tfound by\tsentence\tmention text\targs in all next columns (argType: argText)\n")
    for (file <- parFiles) {
      try {
        val unfiltered = FileUtils.getTextFromFile(file)
        // fixme: temporary, simple text cleanup
        val text = unfiltered.replace("\n",
          " ").replace("- ", "")
        val filename = StringUtils.afterLast(file.getName, '/')
        println(s"going to parse input file: $filename")
        val (_, mentions) = vp.parse(text)
        val contentMentions = mentions.filter(m => m.labels.contains("Event"))
        for (m <- contentMentions) println("Men: " + m.label + " " + m.text)

        for (m <- contentMentions) {
          pw.write(s"${filename}\t${m.label}\t${m.foundBy}\t${m.sentenceObj.getSentenceText}\t${m.text}")
          for (arg <- m.arguments) {
            if (arg._2.nonEmpty) {
              // multiple args of same type are "::"-separated
              pw.write(s"\t${arg._1}:\t${arg._2.map(_.text.trim().replace("\t", "")).mkString("::")}")
            }
          }
          pw.write("\n")
        }

      }
      catch {
        case e: Exception => e.printStackTrace()
      }
    }
    pw.close()

  }
}