package org.clulab.variables

import org.clulab.utils.{FileUtils, StringUtils, outputMentionsToTSV}

import java.io._

object VariableReader {

  def main(args: Array[String]): Unit = {
    val props = StringUtils.argsToProperties(args)

    val inputDir = props.getProperty("in")
    assert(inputDir != null)
    val outputDir = props.getProperty("out")
    assert(outputDir != null)
    val output = new File(outputDir).mkdir()
    val vp = VariableProcessor()

    var seqMention = Seq[String]()
    var outputFile = outputDir+"/mentions.tsv"

    val fw=new FileWriter(new File(outputFile))
    val pw = new PrintWriter(fw)
    for(file <- FileUtils.findFiles(inputDir, ".txt")) {
      val text = FileUtils.getTextFromFile(file)
      val filename = file.toString.split("/").last
      println(s"going to parse input file: $filename")
      val (doc, mentions,context)  = vp.parse(text)
      println(s"Writing mentions from doc ${filename} to $outputFile")
      //todo: simplify context- here you will find sent from location 0 etc
      outputMentionsToTSV(mentions, doc, context, filename, pw)
      // to not overpopulate the memory. Flush findings once for each document.
      pw.flush()
      fw.flush()
    }


    pw.close()
  }
}
