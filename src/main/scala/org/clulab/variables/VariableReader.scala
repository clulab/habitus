package org.clulab.variables

import org.clulab.utils.{FileUtils, StringUtils, outputMentionsToTSV, writeFile}

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

    for(file <- FileUtils.findFiles(inputDir, ".txt")) {
      val text = FileUtils.getTextFromFile(file)
      val (doc, mentions) = vp.parse(text)
      seqMention ++= outputMentionsToTSV(mentions, doc, file.toString.split("/").last)
    }
    assert(seqMention.length >= 1)
    writeFile(outputDir+"/mentions.tsv", seqMention)
  }
}
