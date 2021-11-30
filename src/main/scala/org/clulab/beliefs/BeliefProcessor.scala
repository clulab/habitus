package org.clulab.beliefs

import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import org.apache.commons.io.FileUtils
import org.clulab.dynet.Utils
import org.clulab.habitus.HabitusProcessor
import org.clulab.habitus.actions.HabitusActions
import org.clulab.odin.{EventMention, ExtractorEngine, Mention, RelationMention, State, TextBoundMention}
import org.clulab.openie.entities.CustomizableRuleBasedFinder
import org.clulab.processors.{Document, Processor}
import ujson.IndexedValue.False

import java.io.File
import java.nio.charset.StandardCharsets
import scala.collection.JavaConverters.{asJavaIterableConverter, asScalaBufferConverter}
import scala.util.Try

class BeliefProcessor(val processor: Processor,
                      val entityFinder: CustomizableRuleBasedFinder,
                      val extractor: ExtractorEngine) {

  // fixme: you prob want this to be from a config
  val maxHops: Int = 3

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

  def parse(text: String): (Document, Seq[Mention]) = {
    // pre-processing
    val doc = processor.annotate(text, keepText = false)

    // extract syntactic mentions, without expansion
    val entityMentions = entityFinder.extract(doc)

    // extract mentions from annotated document
    val initialState = State(entityMentions)
    val eventMentions = extractor.extractFrom(doc, initialState).sortBy(m => (m.sentence, m.getClass.getSimpleName))

    // expand the arguments, don't allow to cross the trigger
    val eventTriggers = eventMentions.collect { case em: EventMention => em.trigger }
    val expandedMentions = eventMentions.map(expandArgs(_, State(eventTriggers)))

    // keep only beliefs that have less than 150 tokens and <50% of tokens being numbers
    val shortBeliefMentions = expandedMentions.filter(containsLessThan150Tokens).filter(halfOfTokensAreNumbers)

    // keep only beliefs that look like propositions
    val propBeliefMentions = shortBeliefMentions.filter(containsPropositionBelief)

    (doc, propBeliefMentions)
  }


  //keep only beliefs Sentences with <50% of tokens being numbers
  private def halfOfTokensAreNumbers(m: Mention): Boolean = {
    m.isInstanceOf[EventMention] &&
      m.arguments.contains("belief") &&
      m.arguments("belief").nonEmpty &&
      checkNumberCount(m)
  }

  private def checkNumberCount(mention: Mention): Boolean = {
    var numberCounter = 0
    for (word <- mention.sentenceObj.words) {
      if (Try(word.toInt).isSuccess || Try(word.toFloat).isSuccess || Try(word.toDouble).isSuccess || Try(word.toLong).isSuccess ) {
        numberCounter = numberCounter + 1
      }
  }
  numberCounter < mention.sentenceObj.words.length / 2
}

  //keep only beliefs which have less than 150 tokens
  private def containsLessThan150Tokens(m: Mention): Boolean = {
    m.isInstanceOf[EventMention] &&
      m.arguments.contains("belief") &&
      m.arguments("belief").nonEmpty &&
      (m.sentenceObj.words.length < 150)
  }

  private def containsPropositionBelief(m: Mention): Boolean = {
    m.isInstanceOf[EventMention] &&
      m.arguments.contains("belief") &&
      m.arguments("belief").nonEmpty &&
      isProposition(m.arguments("belief").head)
  }

  /** True if this mention contains a proposition */
  private def isProposition(mention: Mention): Boolean = {
    val sent = mention.sentenceObj
    val tags = sent.tags.get
    val span = mention.tokenInterval

    var nounCount = 0
    var verbCount = 0
    for(i <- span.start until span.end) {
      if(tags(i).startsWith("NN")) nounCount += 1
      else if(tags(i).startsWith("VB")) verbCount += 1
    }

    nounCount > 1 || (nounCount > 0 && verbCount > 0)
  }

}

object BeliefProcessor {
  def apply(): BeliefProcessor = {
    // create the processor
    Utils.initializeDyNet()
    val processor: Processor = new HabitusProcessor(None)

    // the mention finder, without expansion
    val config = ConfigFactory.load()
    val invalidOutgoing = config.getList("CustomRuleBasedEntityFinder.invalidOutgoing").asScala ++ Seq("mark")
    val finder = CustomizableRuleBasedFinder.fromConfig(
      config.withValue(
        "CustomRuleBasedEntityFinder.maxHops",
        ConfigValueFactory.fromAnyRef(0)
      ).withValue(
        "CustomRuleBasedEntityFinder.entityRulesPath",
        ConfigValueFactory.fromAnyRef("beliefs/entities.yml")
      ).withValue(
        "CustomRuleBasedEntityFinder.ensureBaseTagNounVerb",
        ConfigValueFactory.fromAnyRef("false")
      ).withValue(
        "CustomRuleBasedEntityFinder.avoidRulesPath",
        ConfigValueFactory.fromAnyRef("beliefs/avoid.yml")
      ).withValue(
        "CustomRuleBasedEntityFinder.invalidOutgoing",
          ConfigValueFactory.fromAnyRef(invalidOutgoing.asJava)
      )
    )

    BeliefProcessor(processor, finder)
  }

  def apply(processor: Processor, finder: CustomizableRuleBasedFinder): BeliefProcessor = {
    // get current working directory
    val cwd = new File(System.getProperty("user.dir"))
    // Find resource dir from project root.
    val resourceDir = new File(cwd, "src/main/resources")
    val masterFile = new File(resourceDir, "beliefs/master.yml")
    val actions = new HabitusActions
    // We usually want to reload rules during development,
    // so we try to load them from the filesystem first, then jar.
    if (masterFile.exists()) {
      // read file from filesystem
      val rules = FileUtils.readFileToString(masterFile, StandardCharsets.UTF_8)
      // creates an extractor engine using the rules and the default actions
      val extractor = ExtractorEngine(rules, actions, actions.cleanupAction) // , path = Some(resourceDir)) // TODO: do we still need this?
      new BeliefProcessor(processor, finder, extractor)
    } else {
      // read rules from yml file in resources
      val source = io.Source.fromURL(getClass.getResource("/beliefs/master.yml"))
      val rules = source.mkString
      source.close()
      // creates an extractor engine using the rules and the default actions
      val extractor = ExtractorEngine(rules, actions, actions.cleanupAction)
      new BeliefProcessor(processor, finder, extractor)
    }
  }

}
