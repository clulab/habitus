package org.clulab.habitus.variables

import org.clulab.habitus.utils._
import org.clulab.utils.CliReader
import org.clulab.utils.ExitMenuItem
import org.clulab.utils.HelpMenuItem
import org.clulab.utils.Menu
import org.clulab.utils.ReloadableShell
import org.clulab.utils.SafeDefaultMenuItem
import org.clulab.utils.SafeMainMenuItem
import org.clulab.utils.StringUtils

class ReloadableVariableProcessor(val masterResource: String) {
  protected var variableProcessor: VariableProcessor = 
    VariableProcessor(masterResource)

  def get: VariableProcessor = variableProcessor

  def reload(): Unit = variableProcessor = variableProcessor.reloaded
}

/** Interactive shell for variable reading */
class VariableShell(val masterResource: String) extends ReloadableShell {
  println("Creating VariableProcessor...\n")
  private val vp: ReloadableVariableProcessor = new ReloadableVariableProcessor(masterResource)

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
    val parsingResults = vp.get.parse(text)
    val doc = parsingResults.document
    val mentions = parsingResults.allMentions
//    for (m <- mentions) println("shell m: " + m.label + " " + m.text + " " + m.norms.get.mkString("::"))
    val contentMentions = parsingResults.targetMentions

    // note: to see attachment, display contentMentions
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
  val props = StringUtils.argsToMap(args)
  val masterResource = props.getOrElse("grammar", "/variables/master.yml")
  
  new VariableShell(masterResource).shell()
}
