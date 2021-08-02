package org.clulab.variables

import org.clulab.odin.ExtractorEngine
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.fastnlp.FastNLPProcessor
import org.clulab.utils._

class VariableReader {

}

object VariableReader {
  def main(args: Array[String]): Unit = {
    val proc = new CluProcessor()
    val source = io.Source.fromURL(getClass.getResource("/variables/master.yml"))
    val rules = source.mkString
    source.close()
    val extractor = ExtractorEngine(rules)

    val text = "Sowing season is in July."
    val doc = proc.annotate(text)
    val mentions = extractor.extractFrom(doc)
    println(s"Found ${mentions.size} mentions.")
    displayMentions(mentions, doc)
  }
}
