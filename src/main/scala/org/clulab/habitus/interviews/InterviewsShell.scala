package org.clulab.habitus.interviews

import org.clulab.habitus.utils.displayMentions
import org.clulab.utils._

class InterviewsShell extends Shell {
  println("Creating InterviewsProcessor...\n")
  private var bp: InterviewsProcessor = InterviewsProcessor()

  def reload(): Unit = {
    println("Reloading InterviewsProcessor...")
    bp = InterviewsProcessor(bp.processor, bp.entityFinder)
  }

  override def work(text: String): Unit = {
    // the actual reading
    val (doc, mentions) = bp.parse(text)

    // debug display the mentions
    displayMentions(mentions, doc)
  }

  override def mkMenu(): Menu = {
    val lineReader = new CliReader("(Habitus)>>> ", "user.home", ".habitusshellhistory")
    val mainMenuItems = Seq(
      new HelpMenuItem(":help", "show commands"),
      new SimpleMainMenuItem(":reload", "reload the processor", reload _),
      new ExitMenuItem(":exit", "exit system")
    )
    val defaultMenuItem = new SafeDefaultMenuItem(work)

    new Menu("Welcome to the Habitus Shell!", lineReader, mainMenuItems, defaultMenuItem)
  }
}

object InterviewsShell extends App {
  val sh = new InterviewsShell()
  sh.shell()
}

