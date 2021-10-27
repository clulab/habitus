package org.clulab.variables

import org.clulab.utils._

/** Interactive shell for variable reading */
class VariableShell(vp: VariableProcessor) extends Shell {
  override def work(text: String): Unit = {
    // the actual reading
    val (doc, mentions,eventMentions,histogram) = vp.parse(text)

    // debug display the mentions
    displayMentions(mentions, doc)
  }
}

object VariableShell extends App {
  val vp = VariableProcessor()
  val sh = new VariableShell(vp)
  sh.shell()
}

