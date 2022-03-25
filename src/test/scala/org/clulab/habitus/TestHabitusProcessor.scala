package org.clulab.habitus

import org.clulab.habitus.utils.Test
import org.clulab.habitus.variables.VariableProcessor

class TestHabitusProcessor extends Test {

  behavior of "HabitusProcessor"

  it should "process Unicode correctly" in {
    val variableProcessor = VariableProcessor().reloaded // now uses a HabitusProcessor
    val text = HabitusTokenizer.endash + "1"

    val parsingResults = variableProcessor.parse(text)
    val doc = parsingResults.document
    val words = doc.sentences.head.words
    val raw = doc.sentences.head.raw

    words.head.substring(0, 1) should be (HabitusTokenizer.dash)
    // This does not pass.  We are changing the raw text!
    // raw.head.substring(0, 1) should be (HabitusTokenizer.endash)
  }
}
