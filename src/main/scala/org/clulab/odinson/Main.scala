package org.clulab.odinson

import java.io.File
import ai.lum.odinson.ExtractorEngine
import ai.lum.odinson.serialization.JsonSerializer
import ai.lum.common.ConfigFactory
import ai.lum.common.ConfigUtils._
import ai.lum.common.FileUtils._

object Main extends App {

  // load configuration
  val config = ConfigFactory.load()
  val pathToRules = config[String]("habitus.pathToRules")
  val mentionsFile = config[File]("habitus.mentionsFile")

  // make extractor engine with params from config file
  val extractorEngine = ExtractorEngine.fromConfig()
  // compile extractors
  val extractors = extractorEngine.compileRuleResource(pathToRules)
  // extract mentions
  val mentions = extractorEngine.extractMentions(extractors)
  // post-processing step (filtering, linking, ???)
  // TODO
  //val json = JsonSerializer.asJsonString(mentions)
  //mentionsFile.writeString(json)

}
