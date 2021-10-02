package org.clulab.variables

import org.clulab.dynet.Utils
import org.clulab.odin.{ExtractorEngine, Mention}
import org.clulab.processors.{Document, Processor}
import org.clulab.processors.clu.CluProcessor
import org.clulab.sequences.LexiconNER
import org.clulab.utils.contextDetails

class VariableProcessor(val processor: Processor, val extractor: ExtractorEngine) {
  def parse(text: String): (Document, Seq[Mention],scala.collection.mutable.Map[Int,contextDetails]) = {

    // pre-processing
    val doc = processor.annotate(text, keepText = false)

    // extract mentions from annotated document
    val mentions = extractor.extractFrom(doc).sortBy(m => (m.sentence, m.getClass.getSimpleName))

    //extract contexts:
    // map sent-id to event mentions, to each of the best entity locations- useful in printing. until mihai approves eventmention to be modified to hold context
    val sentidContext=scala.collection.mutable.Map[Int,contextDetails]()
    val ce= ContextExtractor()
    ce.parse(doc,mentions,sentidContext)
    (doc, mentions,sentidContext)
  }
}

object VariableProcessor {
  def apply(): VariableProcessor = {
    // Custom NER for variable reading
    val kbs = Seq(
      "variables/FERTILIZER.tsv",
      "variables/CROP.tsv"
    )
    val lexiconNer = LexiconNER(kbs,
      Seq(
        true, // case insensitive match for fertilizers
        true
      )
    )

    // create the processor
    Utils.initializeDyNet()
    val processor: Processor = new CluProcessor(optionalNER = Some(lexiconNer))

    // read rules from yml file in resources
    val source = io.Source.fromURL(getClass.getResource("/variables/master.yml"))
    val rules = source.mkString
    source.close()

    // creates an extractor engine using the rules and the default actions
    val extractor = ExtractorEngine(rules)

    new VariableProcessor(processor, extractor)
  }
}
