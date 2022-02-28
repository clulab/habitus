package org.clulab.habitus.variables

import org.clulab.odin.{EventMention, ExtractorEngine, Mention}
import org.clulab.processors.Document
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.fastnlp.FastNLPProcessor


class EntityProcessor(val processor: FastNLPProcessor, val extractor: ExtractorEngine) {

  def reloaded: EntityProcessor = {
    val newLexiconNer = VariableProcessor.newLexiconNer()
    //val newProcessor = processor.copy(optionalNEROpt = Some(Some(newLexiconNer)))

    val newExtractorEngine = VariableProcessor.newExtractorEngine()

    new EntityProcessor(processor, newExtractorEngine)
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


