package org.clulab.variables

import org.clulab.utils._

/** Interactive shell for variable reading */
class VariableShell() extends Shell {
  println("Creating VariableProcessor...\n")
  private var vp: VariableProcessor = VariableProcessor()

  def reload(): Unit = {
    println("Reloading VariableProcessor...")
    vp = VariableProcessor(vp.processor)
  }

  override def work(text: String): Unit = {
    // the actual reading
    val (doc, mentions) = vp.parse(text)

    // debug display the mentions
    displayMentions(mentions, doc)
  }

  override def mkMenu(): Menu = {
    val lineReader = new CliReader("(Habitus)>>> ", "user.home", ".habitusshellhistory")
    val mainMenuItems = Seq(
      new HelpMenuItem(":help", "show commands"),
      new SimpleMainMenuItem(":reload", "reload the variable processor", reload _),
      new ExitMenuItem(":exit", "exit system")
    )
    val defaultMenuItem = new SafeDefaultMenuItem(work)

    new Menu("Welcome to the Habitus Shell!", lineReader, mainMenuItems, defaultMenuItem)
  }
}

object VariableShell extends App {
  val sh = new VariableShell
  sh.shell()
}
