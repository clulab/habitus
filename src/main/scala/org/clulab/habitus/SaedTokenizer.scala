package org.clulab.habitus


import org.clulab.habitus.variables.VariableReader.run
import org.clulab.processors.Processor
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.clu.tokenizer.Tokenizer
import org.clulab.utils.StringUtils
//
//class SaedTokenizer(tokenizer: Tokenizer) extends Tokenizer(tokenizer.lexer, tokenizer.steps, tokenizer.sentenceSplitter) {
//  val proc: Processor = new CluProcessor()
//  val tokens=proc.mkDocument("This is a test.", keepText = true)
//  println(tokens)
//
//}

object SaedTokenizer {

  def main(args: Array[String]): Unit = {
    val proc: Processor = new CluProcessor()
    val tokens = proc.mkDocument("This is a test.", keepText = true)
    println(tokens)
  }
}


