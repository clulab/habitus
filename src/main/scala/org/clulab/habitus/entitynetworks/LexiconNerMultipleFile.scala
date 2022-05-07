package org.clulab.habitus.entitynetworks

import org.clulab.dynet.Utils
import org.clulab.processors.clu.{CluProcessor, GivenConstEmbeddingsAttachment}
import org.clulab.sequences.LexiconNER
import org.clulab.utils.FileUtils

import java.io.{File, PrintWriter}
import scala.collection.mutable.ArrayBuffer

/** extracts the NER from several given text files in a folder and saves them in the same folder with the suffix: .out. */

object LexiconNerMultipleFile extends App {
  def mkLexiconNer(): LexiconNER = {
    val resourceDir: File = {
      val cwd = new File(System.getProperty("user.dir"))
      new File(cwd, "src/main/resources")
    }

    val kbs = Seq(
      "lexicons/ACRONYM.tsv"
    )
    val isLocal = kbs.forall(new File(resourceDir, _).exists)
    val lexiconNer = LexiconNER(kbs,
      Seq(
        false // case insensitive match for fertilizers
      ),
      if (isLocal) Some(resourceDir) else None
    )

    lexiconNer
  }

  def parseAndPrint(sentence: ArrayBuffer[String], pw: PrintWriter): Unit = {
    val doc = processor.mkDocumentFromTokens(List(sentence))
    GivenConstEmbeddingsAttachment(doc).perform {
      processor.tagPartsOfSpeech(doc)
      processor.recognizeNamedEntities(doc)
    }

    for(sent <- doc.sentences) {
      val words = sent.words
      val entities = sent.entities.get

      for(i <- words.indices) {
        pw.println(words(i) + "\t" + entities(i))
      }
      pw.println("\n")
    }
  }

  val lexiconNer = mkLexiconNer()
  val processor = new CluProcessor(optionalNER = Some(lexiconNer))
  Utils.initializeDyNet()

  val inputDir = args(0)
  val files = FileUtils.findFiles(inputDir, ".txt")

  for(file <- files) { 
    parseFile(file)
 }

  def parseFile(f:File): Unit = {
    val outputFileName = f + ".out"
    val pw = new PrintWriter(outputFileName)
    var sentence = new ArrayBuffer[String]() // tokens in the current sentence
    var lines = scala.io.Source.fromFile(f).getLines()
    for(line <- lines) {
      if(line.trim.isEmpty) {
        parseAndPrint(sentence, pw)
        sentence = new ArrayBuffer[String]()
      } else {
        val bits = line.trim.split("\\s+")
        sentence ++= bits
      }
    }
    if(sentence.nonEmpty) {
      parseAndPrint(sentence, pw)
    }
    pw.close()
  }

}
