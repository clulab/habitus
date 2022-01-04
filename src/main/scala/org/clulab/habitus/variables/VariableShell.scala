package org.clulab.habitus.variables

import org.clulab.habitus.utils._
import org.clulab.utils.CliReader
import org.clulab.utils.ExitMenuItem
import org.clulab.utils.HelpMenuItem
import org.clulab.utils.Menu
import org.clulab.utils.ReloadableShell
import org.clulab.utils.SafeDefaultMenuItem
import org.clulab.utils.SafeMainMenuItem

class ReloadableVariableProcessor() {
  protected var variableProcessor: VariableProcessor = VariableProcessor()

  def get: VariableProcessor = variableProcessor

  def reload(): Unit = variableProcessor = variableProcessor.reloaded
}

/** Interactive shell for variable reading */
class VariableShell() extends ReloadableShell {
  println("Creating VariableProcessor...\n")
  private val vp: ReloadableVariableProcessor = new ReloadableVariableProcessor()

    override def reload(): Unit = {
    println("Reloading VariableProcessor...")
    try {
      vp.reload()
    }
    catch {
      case throwable: Throwable =>
        println(s"The variable processor could not be reloaded!")
        throwable.printStackTrace()
    }
  }

  override def work(text: String): Unit = {
    // the actual reading
    val (doc, mentions, _, _) = vp.get.parse(text)

    // debug display the mentions
    displayMentions(mentions, doc)
  }

  override def mkMenu(): Menu = {
    // val lineReader = new IdeReader("(Habitus)>>> ")
    val lineReader = new CliReader("(Habitus)>>> ", "user.home", ".habitusshellhistory")
    val mainMenuItems = Seq(
      new HelpMenuItem(":help", "show commands"),
      new SafeMainMenuItem(":reload", "reload the variable processor", reload _),
      new ExitMenuItem(":exit", "exit system")
    )
    val defaultMenuItem = new SafeDefaultMenuItem(work)

    new Menu("Welcome to the Habitus Shell!", lineReader, mainMenuItems, defaultMenuItem)
  }
}

object VariableShell extends App {
  new VariableShell().shell()
}
