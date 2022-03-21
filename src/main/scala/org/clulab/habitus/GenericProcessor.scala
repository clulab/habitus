package org.clulab.habitus

import org.clulab.habitus.variables.EntityDistFreq
import org.clulab.odin.Mention
import org.clulab.processors.Document

trait GenericProcessor {
  def parse(text: String): (Document, Seq[Mention], Seq[Mention])
}
