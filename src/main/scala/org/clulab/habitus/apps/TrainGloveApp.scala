package org.clulab.habitus.apps

import org.clulab.processors.clu.CluProcessor
import org.clulab.utils.FileUtils

import java.io.{File, PrintWriter}
import scala.util.Using

object TrainGloveApp extends App {

  def syntax(): String = {
    System.err.println("Syntax: inputDirName outputFileName")
    System.exit(-1)
    "" // trick
  }

  val inputDirName = args.lift(0).getOrElse(syntax())
  val outputFileName = args.lift(1).getOrElse(syntax())

  val processor = new CluProcessor()

  def process(file: File, printWriter: PrintWriter): Unit = {
    println(file.getName) // already synchronized

    val text = FileUtils.getTextFromFile(file)
    val document = processor.mkDocument(text)
    val sentenceTexts = document.sentences.map(_.words.mkString(" "))
    val documentText = sentenceTexts.mkString(" ")

    printWriter.synchronized {
      printWriter.println(documentText)
    }
  }

  Using.resource(FileUtils.printWriterFromFile(outputFileName)) { printWriter =>
    val files = FileUtils.findFiles(inputDirName,"txt").sortBy(_.getName).par

    files.foreach { file =>
      try {
        process(file, printWriter)
      }
      catch {
        case throwable: Throwable => throwable.printStackTrace()
      }
    }
  }
}
