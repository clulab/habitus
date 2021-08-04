package org.clulab.variables

import org.clulab.utils.{FileUtils, StringUtils, displayMentions}

object VariableReader {
  var DEBUG = true

  def main(args: Array[String]): Unit = {
    val props = StringUtils.argsToProperties(args)

    val inputDir = props.getProperty("in")
    assert(inputDir != null)
    val outputDir = props.getProperty("out")
    assert(outputDir != null)

    val vp = VariableProcessor()
    for(file <- FileUtils.findFiles(inputDir, ".txt")) {
      val text = FileUtils.getTextFromFile(file)
      val (doc, mentions) = vp.parse(text)

      //
      // debugging output
      //
      if(DEBUG) {
        println(s"Parsing text:\n$text")
        println("Found mentions of variable assignments:")
        displayMentions(mentions, doc)
      }

      // TODO: normalize and save in TSV format here
    }
  }
}
