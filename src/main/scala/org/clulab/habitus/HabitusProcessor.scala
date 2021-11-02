package org.clulab.habitus

import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.clu.tokenizer.Tokenizer
import org.clulab.sequences.LexiconNER

class HabitusProcessor(lexiconNer: LexiconNER) extends CluProcessor(optionalNER = Some(lexiconNer)) {
  lazy val habitusTokenizer: HabitusTokenizer = new HabitusTokenizer(localTokenizer)
  override lazy val tokenizer: Tokenizer = habitusTokenizer
}
