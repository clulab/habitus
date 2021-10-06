package org.clulab.variables

import org.clulab.utils._

/** Interactive shell for variable reading */
class VariableShell() extends Shell {

  private var vp: VariableProcessor = VariableProcessor()

  override def reload(): Unit = {
    println("reloading VariableProcessor")
    vp = VariableProcessor(vp.processor)
  }

  override def work(text: String): Unit = {
    // the actual reading
    val (doc, mentions) = vp.parse(text)

    // debug display the mentions
    displayMentions(mentions, doc)
  }

}

object VariableShell extends App {
  val sh = new VariableShell
  sh.shell()
}
