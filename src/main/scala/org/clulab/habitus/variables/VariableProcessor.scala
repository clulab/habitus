package org.clulab.habitus.variables
import java.io.File
import org.clulab.dynet.Utils
import org.clulab.habitus.HabitusProcessor
import org.clulab.odin.{EventMention, ExtractorEngine, Mention}
import org.clulab.processors.Document
import org.clulab.processors.clu.CluProcessor
import org.clulab.sequences.LexiconNER
import org.clulab.utils.FileUtils

import scala.util.{Failure, Success, Try}



class VariableProcessor(val processor: CluProcessor, val extractor: ExtractorEngine) {

  def reloaded(): VariableProcessor = {
    val newLexiconNer = VariableProcessor.newLexiconNer()
    val newProcessor = processor.copy(optionalNEROpt = Some(Some(newLexiconNer)))
    val newExtractorEngine = VariableProcessor.newExtractorEngine()

    new VariableProcessor(newProcessor, newExtractorEngine)
  }


  def parse(text: String): (Document, Seq[Mention], Seq[EventMention], Seq[EntityDistFreq]) = {
    // pre-processing
    val doc = processor.annotate(text, keepText = false)

    // extract mentions from annotated document
    val mentions = extractor.extractFrom(doc).sortBy(m => (m.sentence, m.getClass.getSimpleName))

    //get histogram of all entities:
    val ce = EntityHistogramExtractor()
    val (allEventMentions, histogram) = ce.extractHistogramEventMentions(doc, mentions)
    (doc, mentions, allEventMentions, histogram)
  }
}

object VariableProcessor {
  val resourceDir = {
    val cwd = new File(System.getProperty("user.dir"))
    new File(cwd, "src/main/resources")
  }
  val resourcePath = "/variables/master.yml"
  val masterFile = new File(resourceDir, resourcePath.drop(1))

  // Custom NER for variable reading
  def newLexiconNer(): LexiconNER = {
    val kbs = Seq(
      "variables/FERTILIZER.tsv",
      "variables/CROP.tsv"
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

  def newExtractorEngine(): ExtractorEngine = {
    // We usually want to reload rules during development,
    // so we try to load them from the filesystem first, then jar.
    if (masterFile.exists()) {
      // read file from filesystem
      val rules = FileUtils.getTextFromFile(masterFile)
      // creates an extractor engine using the rules and the default actions
      ExtractorEngine(rules, ruleDir = Some(resourceDir))
    }
    else {
      // read rules from yml file in resources
      val rules = FileUtils.getTextFromResource(resourcePath)
      // creates an extractor engine using the rules and the default actions
      ExtractorEngine(rules)
    }
  }

  def apply(): VariableProcessor = {
    // create the processor
    Utils.initializeDyNet()
    val lexiconNer = newLexiconNer()
    val processor = new HabitusProcessor(Some(lexiconNer))
    // val processor = new CluProcessor(optionalNER = Some(lexiconNer))
    VariableProcessor(processor)
  }

  /**
    * Reuse an existing processor
    *
    * @param processor
    * @return
    */
  def apply(processor: CluProcessor): VariableProcessor = {
    new VariableProcessor(processor, newExtractorEngine())
  }
}
