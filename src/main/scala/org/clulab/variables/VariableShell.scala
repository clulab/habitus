package org.clulab.variables

import org.clulab.utils._

/** Interactive shell for variable reading */
object VariableShell extends App {
  println("Creating VariableProcessor...\n")
  private var vp: VariableProcessor = VariableProcessor()

  def reload(menu: Menu, key: String): Boolean = {
    println("Reloading VariableProcessor...")
    vp = VariableProcessor(vp.processor)
    true
  }

  def parse(menu: Menu, text: String): Boolean = {
    // the actual reading
    val (doc, mentions) = vp.parse(text)

    // debug display the mentions
    displayMentions(mentions, doc)
    true
  }

  val lineReader = new CliReader("(Habitus)>>> ", "user.home", ".habitusshellhistory")
  val mainMenuItems = Seq(
    new HelpMenuItem(":help", "show commands"),
    new MainMenuItem(":reload", "reload the variable processor", reload),
    new ExitMenuItem(":exit", "exit system")
  )
  val defaultMenuItem = new DefaultMenuItem(parse)
  val menu = new Menu("Welcome to the Habitus Shell!", lineReader, mainMenuItems, defaultMenuItem)

  menu.run()
}
