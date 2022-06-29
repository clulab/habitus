package org.clulab.habitus.beliefs

import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import org.apache.commons.io.FileUtils
import org.clulab.dynet.Utils
import org.clulab.habitus.{GenericProcessor, HabitusProcessor, ParsingResult}
import org.clulab.habitus.actions.HabitusActions
import org.clulab.habitus.utils.{ArrayView, BeliefContext}
import org.clulab.habitus.variables.VariableProcessor.resourceDir
import org.clulab.odin.{EventMention, ExtractorEngine, Mention, RelationMention, State, TextBoundMention}
import org.clulab.openie.entities.CustomizableRuleBasedFinder
import org.clulab.processors.{Document, Processor}
import org.clulab.sequences.LexiconNER
import org.clulab.struct.Interval

import java.io.File
import java.nio.charset.StandardCharsets
import scala.collection.JavaConverters.{asJavaIterableConverter, asScalaBufferConverter}
import scala.collection.mutable.ArrayBuffer


class BeliefProcessor(val processor: Processor,
                      val entityFinder: CustomizableRuleBasedFinder,
                      val extractor: ExtractorEngine) extends GenericProcessor {

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
    val doc = processor.annotate(text, keepText = false)

    // extract syntactic mentions, without expansion
    val entityMentions = entityFinder.extract(doc)

    // extract mentions from annotated document
    val initialState = State(entityMentions)
    val eventMentions = extractor.extractFrom(doc, initialState).sortBy(m => (m.sentence, m.getClass.getSimpleName))
    // expand the arguments, don't allow to cross the trigger
    val eventTriggers = eventMentions.collect { case em: EventMention => em.trigger }
    val expandedMentions = eventMentions.map(expandArgs(_, State(eventTriggers)))
    // keep only beliefs that look like propositions
    val propBeliefMentions = expandedMentions.filter(m => containsPropositionBelief(m) || containsPropositionBeliefWithTheme(m) || m.arguments.size > 2)
    val triggerFilered = triggerBetweenBelieverAndBelief(propBeliefMentions)
    val withAttachment = triggerFilered.map(_.withAttachment(BeliefContext("N/A")))

    ParsingResult(doc, entityMentions ++ expandedMentions, withAttachment.distinct)
  }


  def triggerBetweenBelieverAndBelief(mentions: Seq[Mention]): Seq[Mention] = {
    // filters out belief mentions where believer and belief are not on different sides of the trigger
    val triggerInBetween = new ArrayBuffer[Mention]()
    val (believerAndBeliefMentions, other) = mentions.partition { m =>
      val args = m.arguments.keys.toList
      args.contains("belief") && args.contains("believer")
    }
    for (m <- believerAndBeliefMentions) {
      val triggerSpan = m.asInstanceOf[EventMention].trigger.tokenInterval
      val args = m.arguments
      if (args("believer").head.tokenInterval.start < triggerSpan.start && args("belief").head.tokenInterval.end > triggerSpan.end) {
        triggerInBetween.append(m)
      }
    }
    triggerInBetween ++ other
  }

  def hasArguments(mention: Mention, keys: String*): Boolean =
      keys.forall(mention.arguments.get(_).exists(_.nonEmpty))


  private def containsPropositionBelief(m: Mention): Boolean = {
    m.isInstanceOf[EventMention] &&
      hasArguments(m, "belief") &&
      isProposition(m.arguments("belief").head)
  }

  private def containsPropositionBeliefWithTheme(m: Mention): Boolean = {
    m.isInstanceOf[EventMention] &&
      hasArguments(m, "belief", "beliefTheme") &&
      isPropositionWithTheme(m.arguments("belief").head, m.arguments("beliefTheme").head)
  }

  def countStartsWith(strings: Array[String], prefix: String, tokenIntervals: Interval*): Int =
      tokenIntervals.foldLeft(0) { (sum, tokenInterval) =>
        sum + ArrayView(strings, tokenInterval.start, tokenInterval.end).count(_.startsWith(prefix))
      }

  /** True if this mention contains a proposition */
  private def isProposition(mention: Mention): Boolean = {
    val tags = mention.sentenceObj.tags.get
    val span = mention.tokenInterval
    val nounCount = countStartsWith(tags, "NN", span)
    val verbCount = countStartsWith(tags, "VB", span)
    nounCount > 1 || (nounCount > 0 && verbCount > 0)
  }

  private def isPropositionWithTheme(belief: Mention, beliefTheme: Mention): Boolean = {
    // accounts for examples like <Belief theme> is believed to <belief>, e.g., `This politician is believed to be a narcissist.`
    val tags = belief.sentenceObj.tags.get
    val beliefSpan = belief.tokenInterval
    val beliefThemeSpan = beliefTheme.tokenInterval
    val nounCount = countStartsWith(tags, "NN", beliefSpan, beliefThemeSpan)
    val verbCount = countStartsWith(tags, "VB", beliefSpan, beliefThemeSpan)
    nounCount > 1 || (nounCount > 0 && verbCount > 0)
  }

}

object BeliefProcessor {

  // Custom NER for variable reading
  def newLexiconNer(): LexiconNER = {
    val kbs = Seq(
      "lexicons/ACTOR.tsv"
    )
    val isLocal = kbs.forall(new File(resourceDir, _).exists)
    val lexiconNer = LexiconNER(kbs,
      Seq(
        true // case insensitive match for actors
      ),
      if (isLocal) Some(resourceDir) else None
    )

    lexiconNer
  }

  def apply(): BeliefProcessor = {
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
