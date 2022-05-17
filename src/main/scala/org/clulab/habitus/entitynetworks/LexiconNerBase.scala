package org.clulab.habitus.entitynetworks

import org.clulab.dynet.Utils
import org.clulab.processors.clu.{CluProcessor, GivenConstEmbeddingsAttachment}
import org.clulab.sequences.LexiconNER
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.Sourcer

import java.io.{File, PrintWriter}
import scala.io.Source

class LexiconNerBase() {
  val lexiconNer: LexiconNER = mkLexiconNer()
  val processor = new CluProcessor(optionalNER = Some(lexiconNer))
  Utils.initializeDyNet()

  def mkLexiconNer(): LexiconNER = {
    val resourceDir: File = {
      val cwd = new File(System.getProperty("user.dir"))
      new File(cwd, "src/main/resources")
    }
    val kbs = Seq("lexicons/ACRONYM.tsv")
    val isLocal = kbs.forall(new File(resourceDir, _).exists)
    val lexiconNer = LexiconNER(kbs,
      Seq(false), // case insensitive match for fertilizers
      if (isLocal) Some(resourceDir) else None
    )

    lexiconNer
  }

  def parseAndPrint(sentence: Array[String], printWriter: PrintWriter): Unit = {
    val doc = processor.mkDocumentFromTokens(List(sentence))

    GivenConstEmbeddingsAttachment(doc).perform {
      processor.tagPartsOfSpeech(doc)
      processor.recognizeNamedEntities(doc)
    }
    for (sentence <- doc.sentences) {
      sentence.words.zip(sentence.entities.get).foreach { case (word, entity) =>
        printWriter.println(s"$word\t$entity")
      }
      printWriter.println("\n")
    }
  }

  def process(source: Source, printWriter: PrintWriter): Unit = {
    val lines = source.getLines().map(_.trim)

    while (lines.hasNext) {
      val sentence = lines.takeWhile(_.nonEmpty).flatMap(_.split("\\s+")).toArray

      if (sentence.nonEmpty)
        parseAndPrint(sentence, printWriter)
    }
  }

  def parseFile(inputFile: File): Unit = {
    val outputFileName = inputFile + ".out"

    new PrintWriter(outputFileName).autoClose { printWriter =>
      Sourcer.sourceFromFile(inputFile).autoClose { source =>
        process(source, printWriter)
      }
    }
  }
}
