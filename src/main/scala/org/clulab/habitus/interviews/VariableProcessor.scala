package org.clulab.habitus.interviews

import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import org.apache.commons.io.FileUtils
import org.clulab.dynet.Utils
import org.clulab.habitus.{GenericProcessor, HabitusProcessor, ParsingResult}
import org.clulab.habitus.actions.HabitusActions
import org.clulab.habitus.variables.VariableProcessor.resourceDir
import org.clulab.odin._
import org.clulab.openie.entities.CustomizableRuleBasedFinder
import org.clulab.processors.{Document, Processor}
import org.clulab.sequences.LexiconNER
import org.clulab.wm.eidos.{EidosSystem, SimpleEidos}

import java.io.File
import java.nio.charset.StandardCharsets
import scala.collection.JavaConverters.{asJavaIterableConverter, asScalaBufferConverter}


class VariableProcessor(val processor: Processor,
                        val entityFinder: CustomizableRuleBasedFinder,
                        val extractor: ExtractorEngine,
                        val causationExtractor: EidosSystem) extends GenericProcessor {

  // fixme: you prob want this to be from a config
  val maxHops: Int = 5

  def expandArgs(m: Mention, avoid: State): Mention = {
    def getExpandedArgs(args: Map[String, Seq[Mention]]): Map[String, Seq[Mention]] = {
      for {
        (name, argMentions) <- args
      } yield (name, argMentions.map(m => entityFinder.expand(m, maxHops, avoid)))
    }

    m match {
      case _: TextBoundMention => m // tbms have no args
      case rm: RelationMention => rm.copy(arguments = getExpandedArgs(m.arguments))
      case em: EventMention => em.copy(arguments = getExpandedArgs(m.arguments))
      case _ => ???
    }
  }

  def parse(text: String): ParsingResult = {
    // pre-processing
    val doc = processor.annotate(text, keepText = true)

    // extract syntactic mentions, without expansion
    val entityMentions = entityFinder.extract(doc)

    // extract mentions from annotated document
    val initialState = State(entityMentions)
    val eventMentions = extractor.extractFrom(doc, initialState).sortBy(m => (m.sentence, m.getClass.getSimpleName))
    // expand the arguments, don't allow to cross the trigger
    val eventTriggers = eventMentions.collect { case em: EventMention => em.trigger }
    val expandedMentions = eventMentions.map(expandArgs(_, State(eventTriggers)))
    val causalMentions = causationExtractor.extractFromDoc(doc).allOdinMentions
    ParsingResult(doc, entityMentions ++ expandedMentions, (expandedMentions ++ causalMentions).distinct)
  }


  def hasArguments(mention: Mention, keys: String*): Boolean =
      keys.forall(mention.arguments.get(_).exists(_.nonEmpty))
}


object VariableProcessor {

  // Custom NER for variable reading
  def newLexiconNer(): LexiconNER = {
    val kbs = Seq(
      "lexicons/ACTOR.tsv"
    )
    val isLocal = kbs.forall(new File(resourceDir, _).exists)
    val lexiconNer = LexiconNER(kbs,
      Seq(
        true, // case insensitive
        true
      ),
      if (isLocal) Some(resourceDir) else None
    )

    lexiconNer
  }

  def apply(): VariableProcessor = {
    // create the processor
    Utils.initializeDyNet()
    val lexiconNER = newLexiconNer()
    val processor: Processor = new HabitusProcessor(Some(lexiconNER))

    // the mention finder, without expansion
    val config = ConfigFactory.load()
    val invalidOutgoing = config.getList("CustomRuleBasedEntityFinder.invalidOutgoing").asScala ++ Seq("mark")
    val finder = CustomizableRuleBasedFinder.fromConfig(
      config.withValue(
        "CustomRuleBasedEntityFinder.maxHops",
        ConfigValueFactory.fromAnyRef(0)
      ).withValue(
        "CustomRuleBasedEntityFinder.entityRulesPath",
        ConfigValueFactory.fromAnyRef("interviews/entities.yml")
      ).withValue(
        "CustomRuleBasedEntityFinder.ensureBaseTagNounVerb",
        ConfigValueFactory.fromAnyRef("false")
      ).withValue(
        "CustomRuleBasedEntityFinder.avoidRulesPath",
        ConfigValueFactory.fromAnyRef("interviews/avoid.yml")
      ).withValue(
        "CustomRuleBasedEntityFinder.invalidOutgoing",
        ConfigValueFactory.fromAnyRef(invalidOutgoing.asJava)
      )
    )

    VariableProcessor(processor, finder)
  }

  def apply(processor: Processor, finder: CustomizableRuleBasedFinder): VariableProcessor = {
    // get current working directory
    val cwd = new File(System.getProperty("user.dir"))
    // Find resource dir from project root.
    val resourceDir = new File(cwd, "src/main/resources")
    val masterFile = new File(resourceDir, "interviews/master.yml")
    val actions = new HabitusActions
    // We usually want to reload rules during development,
    // so we try to load them from the filesystem first, then jar.
    if (masterFile.exists()) {
      // read file from filesystem
      val rules = FileUtils.readFileToString(masterFile, StandardCharsets.UTF_8)
      // creates an extractor engine using the rules and the default actions
      val extractor = ExtractorEngine(rules, actions, actions.cleanupAction) // , path = Some(resourceDir)) // TODO: do we still need this?
      val causationExtractor = SimpleEidos(useGeoNorm = false, useTimeNorm = false)
      new VariableProcessor(processor, finder, extractor, causationExtractor)
    } else {
      // read rules from yml file in resources
      val source = io.Source.fromURL(getClass.getResource("/interviews/master.yml"))
      val rules = source.mkString
      source.close()
      // creates an extractor engine using the rules and the default actions
      val extractor = ExtractorEngine(rules, actions, actions.cleanupAction)
      val causationExtractor = SimpleEidos(useGeoNorm = false, useTimeNorm = false)
      new VariableProcessor(processor, finder, extractor, causationExtractor)
    }
  }

}
