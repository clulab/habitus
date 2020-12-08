package org.clulab.habitus

import ai.lum.odinson.ExtractorEngine
import ai.lum.common.ConfigFactory
import ai.lum.common.ConfigUtils._

object Main extends App {

  // load configuration
  val config = ConfigFactory.load()
  val pathToRules = config[String]("habitus.pathToRules")

  // make extractor engine with params from config file
  val extractorEngine = ExtractorEngine.fromConfig()
  // compile extractors
  val extractors = extractorEngine.compileRuleResource(pathToRules)
  // extract mentions
  val mentions = extractorEngine.extractMentions(extractors)
  // post-processing step (filtering, linking, ???)
  // TODO
  // export/serialize
  // TODO


}
