package org.clulab.variables

import java.io.File
import java.nio.charset.StandardCharsets
import org.apache.commons.io.FileUtils
import org.clulab.dynet.Utils
import org.clulab.odin.{ExtractorEngine, Mention}
import org.clulab.processors.{Document, Processor}
import org.clulab.processors.clu.CluProcessor
import org.clulab.sequences.LexiconNER

class VariableProcessor(val processor: Processor, val extractor: ExtractorEngine) {
  def parse(text: String): (Document, Seq[Mention]) = {

    // pre-processing
    val doc = processor.annotate(text, keepText = false)

    // extract mentions from annotated document
    val mentions = extractor.extractFrom(doc).sortBy(m => (m.sentence, m.getClass.getSimpleName))

    (doc, mentions)
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
    VariableProcessor(processor)
  }

  /**
    * Reuse an existing processor
    *
    * @param processor
    * @return
    */
  def apply(processor: Processor): VariableProcessor = {
    // get current working directory
    val cwd = new File(System.getProperty("user.dir"))
    // Find resource dir from project root.
    val resource_dir = new File(cwd, "src/main/resources")
    val master_file = new File(resource_dir, "variables/master.yml")
    // We usually want to reload rules during development,
    // so we try to load them from the filesystem first, then jar.
    if (master_file.exists()) {
      // read file from filesystem
      val rules = FileUtils.readFileToString(master_file, StandardCharsets.UTF_8)
      // creates an extractor engine using the rules and the default actions
      val extractor = ExtractorEngine(rules) // , path = Some(resource_dir)) // TODO: do we still need this?
      new VariableProcessor(processor, extractor)
    } else {
      // read rules from yml file in resources
      val source = io.Source.fromURL(getClass.getResource("/variables/master.yml"))
      val rules = source.mkString
      source.close()
      // creates an extractor engine using the rules and the default actions
      val extractor = ExtractorEngine(rules)
      new VariableProcessor(processor, extractor)
    }
  }

}
