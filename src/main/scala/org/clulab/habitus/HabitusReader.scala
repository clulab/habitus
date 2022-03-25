package org.clulab.habitus

import ai.lum.common.ConfigUtils._
import com.typesafe.config.{Config, ConfigFactory}
import org.clulab.habitus.beliefs.BeliefProcessor
import org.clulab.habitus.interviews.InterviewsProcessor
import org.clulab.habitus.utils._
import org.clulab.habitus.variables.VariableProcessor
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.{FileUtils, StringUtils, ThreadUtils}

import java.io.File

class HabitusReader() extends App {

  val config = ConfigFactory.load()
  val inputDir: String = config[String]("inputDir")
  val outputDir: String = config[String]("outputDir")
  val threads: Int = config[Int]("threads")
  val factuality: Boolean = config[Boolean]("factuality")


  def run(processor: GenericProcessor, inputDir: String, outputDir: String, threads: Int, masterResource: String, printVariables: PrintVariables): Unit = {
    new File(outputDir).mkdir()

    def mkOutputFile(extension: String): String = outputDir + "/mentions" + extension

//    val selectedProcessor = processor match {
//      case VariableProcessor => VariableProcessor(masterResource)
//      case BeliefProcessor => BeliefProcessor()
//      case InterviewsProcessor => InterviewsProcessor()
//      case _ => ???
//    }
//    val processor = VariableProcessor()
    val files = FileUtils.findFiles(inputDir, ".txt")
    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files

    new MultiPrinter(
      Lazy{new TsvPrinter(mkOutputFile(".tsv"))},
      Lazy(new JsonlPrinter(mkOutputFile(".jsonl")))
    ).autoClose { multiPrinter =>
      for (file <- parFiles) {
        try {
          val text = FileUtils.getTextFromFile(file)
          val filename = StringUtils.afterLast(file.getName, '/')
          println(s"going to parse input file: $filename")
          val parsingResults = processor.parse(text)
          val doc = parsingResults.document
          val targetMentions = parsingResults.targetMentions
          multiPrinter.outputMentions(targetMentions, doc, filename, printVariables)
        }
        catch {
          case e: Exception => e.printStackTrace()
        }
      }
    }
  }

}
