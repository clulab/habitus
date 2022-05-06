package org.clulab.habitus.interviews

import org.clulab.habitus.utils.displayMentions
import org.clulab.utils._

class InterviewsShell extends Shell {
  println("Creating InterviewsProcessor...\n")
  private var interviewsProcessor: VariableProcessor = VariableProcessor()

  def reload(): Unit = {
    println("Reloading InterviewsProcessor...")
    interviewsProcessor = VariableProcessor(interviewsProcessor.processor, interviewsProcessor.entityFinder)
  }

  override def work(text: String): Unit = {
    // the actual reading
    val parsingResults = interviewsProcessor.parse(text)
    val doc = parsingResults.document
    val targetMentions = parsingResults.targetMentions


    // debug display the mentions
    displayMentions(targetMentions, doc)
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

