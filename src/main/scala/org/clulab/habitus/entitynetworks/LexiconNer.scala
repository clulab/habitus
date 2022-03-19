package org.clulab.habitus.entitynetworks

import org.clulab.dynet.Utils
import org.clulab.processors.clu.{CluProcessor, GivenConstEmbeddingsAttachment}
import org.clulab.sequences.LexiconNER

import java.io.{File, PrintWriter}
import scala.collection.mutable.ArrayBuffer
import org.clulab.utils.StringUtils
import org.clulab.utils.FileUtils

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

  def parseAndPrint(text: String, pw: PrintWriter): Unit = {
    val doc = processor.mkDocument(text)
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

  val props = StringUtils.argsToMap(args)
  val inputDir = props("in")
  val outputDir = props("out")

  val lexiconNer = mkLexiconNer()
  val processor = new CluProcessor(optionalNER = Some(lexiconNer))
  Utils.initializeDyNet()

  new File(outputDir).mkdir()
  val files = FileUtils.findFiles(inputDir, ".txt")

  for(file <- files) {
    val pw = new PrintWriter(outputDir + File.separator + file.getName() + ".tsv")
    try {
      val text = FileUtils.getTextFromFile(file)
      parseAndPrint(text, pw)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    pw.close()
  }  
}
