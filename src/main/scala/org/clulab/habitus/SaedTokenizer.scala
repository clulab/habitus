package org.clulab.habitus


import org.clulab.habitus.utils.{ContextDetails, JsonlPrinter, Lazy, MultiPrinter, PrintVariables, TsvPrinter}
import org.clulab.habitus.variables.VariableProcessor
import org.clulab.habitus.variables.VariableReader.{compressContext, run}
import org.clulab.processors.Processor
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.clu.tokenizer.Tokenizer
import org.clulab.utils.{FileUtils, StringUtils, ThreadUtils}

import java.io.File
import scala.collection.mutable
//
//class SaedTokenizer(tokenizer: Tokenizer) extends Tokenizer(tokenizer.lexer, tokenizer.steps, tokenizer.sentenceSplitter) {
//  val proc: Processor = new CluProcessor()
//  val tokens=proc.mkDocument("This is a test.", keepText = true)
//  println(tokens)
//
//}

object SaedTokenizer {

  def main(args: Array[String]): Unit = {

    val props = StringUtils.argsToMap(args)
    val inputDir = props("in")
    val outputDir = props("out")
    val threads = props.get("threads").map(_.toInt).getOrElse(1)

    run(inputDir, outputDir, threads)


  }


  def run(inputDir: String, outputDir: String, threads: Int): Unit = {
    new File(outputDir).mkdir()

    def mkOutputFile(extension: String): String = outputDir + "/mentions" + extension

    val proc: Processor = new CluProcessor()
    val files = FileUtils.findFiles(inputDir, ".txt")
    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files

    import java.io.FileWriter
    val fw = new FileWriter("out.txt")
    for (file <- parFiles) {
      try {
        val text = FileUtils.getTextFromFile(file)
        val filename = StringUtils.afterLast(file.getName, '/')
        println(s"going to parse input file: $filename")
        val doc = proc.mkDocument(text, keepText = true)
        for (s <- doc.sentences) {
          for (word <- s.words) {
            fw.write(word+"\n")
          }
        }
      }

      catch {
        case e: Exception => e.printStackTrace()
      }

    }
  }
}


