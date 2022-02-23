package org.clulab.habitus.entitynetworks

import org.clulab.dynet.Utils
import org.clulab.processors.clu.{CluProcessor, GivenConstEmbeddingsAttachment}
import org.clulab.sequences.LexiconNER

import java.io.{File, PrintWriter}
import scala.collection.mutable.ArrayBuffer

object LexiconNer extends App {
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
      pw.println()
    }
  }

  val lexiconNer = mkLexiconNer()
  val processor = new CluProcessor(optionalNER = Some(lexiconNer))
  Utils.initializeDyNet()

  val inputFileName = args(0)
  val outputFileName = args(0) + ".out"

  val pw = new PrintWriter(outputFileName)
  var sentence = new ArrayBuffer[String]() // tokens in the current sentence
  for(line <- io.Source.fromFile(new File(inputFileName)).getLines()) {
    if(line.trim.isEmpty) {
      parseAndPrint(sentence, pw)
      sentence = new ArrayBuffer[String]()
    } else {
      val bits = line.trim.split("\\s+")
      sentence += bits(0)
    }
  }
  if(sentence.nonEmpty) {
    parseAndPrint(sentence, pw)
  }
  pw.close()
}
