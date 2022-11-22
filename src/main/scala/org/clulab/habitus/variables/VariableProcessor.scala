package org.clulab.habitus.variables

import org.clulab.dynet.Utils
import org.clulab.habitus.HabitusProcessor
import org.clulab.habitus.actions.HabitusActions
import org.clulab.habitus.utils.{ContextExtractor, DefaultContextExtractor}
import org.clulab.odin.{EventMention, ExtractorEngine, Mention}
import org.clulab.processors.Document
import org.clulab.processors.clu.CluProcessor
import org.clulab.sequences.LexiconNER
import org.clulab.utils.FileUtils

import java.io.File

class VariableProcessor(val processor: CluProcessor, 
  val extractor: ExtractorEngine,
  val contextExtractor: DefaultContextExtractor,
  val masterResource: String) {

  def reloaded: VariableProcessor = {
    val newLexiconNer = VariableProcessor.newLexiconNer()
    val newProcessor = processor.copy(optionalNEROpt = Some(Some(newLexiconNer)))
    val newExtractorEngine = VariableProcessor.newExtractorEngine(masterResource)

    new VariableProcessor(newProcessor, newExtractorEngine, contextExtractor, masterResource)
  }


  def parse(text: String): (Document, Seq[Mention], Seq[Mention], Seq[EntityDistFreq]) = {
    // pre-processing
    val doc = processor.annotate(text, keepText = true)
    val actions = new HabitusActions
    // extract mentions from annotated document
    val mentions = extractor.extractFrom(doc).sortBy(m => (m.sentence, m.getClass.getSimpleName))

    //get histogram of all entities:
    val ce = EntityHistogramExtractor()
    val (allEventMentions, histogram) = ce.extractHistogramEventMentions(doc, mentions)
    val contentMentionsWithContexts = contextExtractor.getContextPerMention(mentions, histogram, doc, "Assignment")

    (doc, mentions.distinct, contentMentionsWithContexts, histogram)
  }

  def parse(doc: Document): (Document, Seq[Mention], Seq[Mention], Seq[EntityDistFreq]) = {
    // pre-processing
    val actions = new HabitusActions
    // extract mentions from annotated document
    val mentions = extractor.extractFrom(doc).sortBy(m => (m.sentence, m.getClass.getSimpleName))

    //get histogram of all entities:
    val ce = EntityHistogramExtractor()
    val (allEventMentions, histogram) = ce.extractHistogramEventMentions(doc, mentions)
    val contentMentionsWithContexts = contextExtractor.getContextPerMention(mentions, histogram, doc, "Assignment")

    (doc, mentions, contentMentionsWithContexts, histogram)
  }
}

object VariableProcessor {
  val resourceDir: File = {
    val cwd = new File(System.getProperty("user.dir"))
    new File(cwd, "src/main/resources")
  }

  // Custom NER for variable reading
  def newLexiconNer(): LexiconNER = {
    val kbs = Seq(
      "lexicons/FERTILIZER.tsv",
      "lexicons/CROP.tsv"
    )
    val isLocal = kbs.forall(new File(resourceDir, _).exists)
    val lexiconNer = LexiconNER(kbs,
      Seq(
        true, // case insensitive match for fertilizers
        true
      ),
      if (isLocal) Some(resourceDir) else None
    )

    lexiconNer
  }

  def newExtractorEngine(masterResource: String): ExtractorEngine = {
    // We usually want to reload rules during development,
    // so we try to load them from the filesystem first, then jar.
    val masterFile = new File(resourceDir, masterResource.drop(1)) // the resource path must start with /
    if (masterFile.exists()) {
      // read file from filesystem
      val rules = FileUtils.getTextFromFile(masterFile)
      val actions = new HabitusActions
      // creates an extractor engine using the rules and the default actions
      ExtractorEngine(rules, actions, actions.cleanupAction, ruleDir = Some(resourceDir))
    }
    else {
      // read rules from yml file in resources
      val rules = FileUtils.getTextFromResource(masterResource)
      // creates an extractor engine using the rules and the default actions
      ExtractorEngine(rules)
    }
  }

  def apply(masterResource: String = "/variables/master.yml"): VariableProcessor = {
    assert(masterResource.startsWith("/"))

    // create the processor
    Utils.initializeDyNet()
    val lexiconNer = newLexiconNer()
    val processor = new HabitusProcessor(Some(lexiconNer))
    // val processor = new CluProcessor(optionalNER = Some(lexiconNer))
    VariableProcessor(processor, masterResource)
  }

  /**
    * Reuse an existing processor
    *
    * @param processor
    * @return
    */
  def apply(processor: CluProcessor, masterResource: String): VariableProcessor = {
    val contextExtractor = new DefaultContextExtractor()
    new VariableProcessor(processor, newExtractorEngine(masterResource), contextExtractor, masterResource)
  }
}
