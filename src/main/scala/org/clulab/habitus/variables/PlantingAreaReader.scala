package org.clulab.habitus.variables

import ai.lum.common.ConfigUtils._
import com.typesafe.config.{Config, ConfigFactory}
import org.clulab.habitus.{GenericProcessor, HabitusReader}
import org.clulab.habitus.utils._
import org.clulab.odin.Mention
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.{FileUtils, StringUtils, ThreadUtils}

import java.io.File

object PlantingAreaReader extends HabitusReader {

  val masterResource: String = config[String]("PlantingAreaReader.masterResource")
  val label: String = config[String]("PlantingAreaReader.label")
  val mentionArgs: List[String] = config[List[String]]("PlantingAreaReader.args")
  val printVariables = PrintVariables(label, mentionArgs.head, mentionArgs.last)
  run(VariableProcessor, inputDir, outputDir, threads, masterResource, printVariables)



//  def main(args: Array[String]): Unit = {
//    val props = StringUtils.argsToMap(args)
//    val inputDir = props("in")
//    val outputDir = props("out")
//    val masterResource = props.getOrElse("grammar", "/variables/master-areas.yml")
//    val threads = props.get("threads").map(_.toInt).getOrElse(1)
//
//    run(inputDir, outputDir, threads, masterResource)
//  }
//
//  def run(inputDir: String, outputDir: String, threads: Int, masterResource: String): Unit = {
//    new File(outputDir).mkdir()
//
//    def mkOutputFile(extension: String): String = outputDir + "/areas_march14" + extension
//
//    val vp = VariableProcessor(masterResource)
//    val files = FileUtils.findFiles(inputDir, ".txt")
//    val parFiles = if (threads > 1) ThreadUtils.parallelize(files, threads) else files
//
//    new MultiPrinter(
//      Lazy{new TsvPrinter(mkOutputFile(".tsv"))},
//      Lazy(new JsonlPrinter(mkOutputFile(".jsonl")))
//    ).autoClose { multiPrinter =>
//      for (file <- parFiles) {
//        try {
//          val text = FileUtils.getTextFromFile(file).replace(";", ".")
//          val filename = StringUtils.afterLast(file.getName, '/')
//          println(s"going to parse input file: $filename")
//          val (doc, mentions, allEventMentions, entityHistogram) = vp.parse(text)
//          val printVars = PrintVariables("Assignment", "variable", "value")
//          val withoutNegValues = filterNegativeValues(allEventMentions)
//          multiPrinter.outputMentions(withoutNegValues, doc, filename, printVars)
//        }
//        catch {
//          case e: Exception => e.printStackTrace()
//        }
//      }
//    }
//  }
//
//
//  //todo: this should go in the processor?
//  def filterNegativeValues(mentions: Seq[Mention]): Seq[Mention] = {
//    mentions.filterNot(_.arguments("value").head.norms.head.head.startsWith("-"))
//  }

}
