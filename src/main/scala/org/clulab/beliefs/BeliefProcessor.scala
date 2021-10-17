package org.clulab.beliefs

import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import org.apache.commons.io.FileUtils
import org.clulab.dynet.Utils
import org.clulab.odin.{ExtractorEngine, Mention, State}
import org.clulab.openie.entities.{CustomizableRuleBasedFinder, RuleBasedEntityFinder}
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.{Document, Processor}
import org.clulab.utils

import java.io.File
import java.nio.charset.StandardCharsets

class BeliefProcessor(val processor: Processor,
                      val entityFinder: RuleBasedEntityFinder,
                      val extractor: ExtractorEngine) {
  def parse(text: String): (Document, Seq[Mention]) = {

    // pre-processing
    val doc = processor.annotate(text, keepText = false)

    // extract syntactic mentions, without expansion
    val entityMentions = entityFinder.extract(doc)
    //println("Mentions from the entityFinder:")
    //utils.displayMentions(entityMentions, doc)

    // extract mentions from annotated document
    val initialState = State(entityMentions)
    val eventMentions = extractor.extractFrom(doc, initialState).sortBy(m => (m.sentence, m.getClass.getSimpleName))

    (doc, eventMentions)
  }
}

object BeliefProcessor {
  def apply(): BeliefProcessor = {
    // create the processor
    Utils.initializeDyNet()
    val processor: Processor = new CluProcessor()

    // the mention finder, without expansion
    val config = ConfigFactory.load()
    val finder = CustomizableRuleBasedFinder.fromConfig(
      config.withValue(
        "CustomRuleBasedEntityFinder.maxHops",
        ConfigValueFactory.fromAnyRef(0)
      ).withValue(
        "CustomRuleBasedEntityFinder.entityRulesPath",
        ConfigValueFactory.fromAnyRef("beliefs/entities.yml")
      )
    )

    BeliefProcessor(processor, finder)
  }

  def apply(processor: Processor, finder: RuleBasedEntityFinder): BeliefProcessor = {
    // get current working directory
    val cwd = new File(System.getProperty("user.dir"))
    // Find resource dir from project root.
    val resourceDir = new File(cwd, "src/main/resources")
    val masterFile = new File(resourceDir, "beliefs/master.yml")
    // We usually want to reload rules during development,
    // so we try to load them from the filesystem first, then jar.
    if (masterFile.exists()) {
      // read file from filesystem
      val rules = FileUtils.readFileToString(masterFile, StandardCharsets.UTF_8)
      // creates an extractor engine using the rules and the default actions
      val extractor = ExtractorEngine(rules) // , path = Some(resourceDir)) // TODO: do we still need this?
      new BeliefProcessor(processor, finder, extractor)
    } else {
      // read rules from yml file in resources
      val source = io.Source.fromURL(getClass.getResource("/beliefs/master.yml"))
      val rules = source.mkString
      source.close()
      // creates an extractor engine using the rules and the default actions
      val extractor = ExtractorEngine(rules)
      new BeliefProcessor(processor, finder, extractor)
    }
  }

}
