package org.clulab.habitus.scraper.apps

import org.clulab.habitus.scraper.utils.TsvReader

import scala.io.Source
import scala.util.Using

object CheckNaka extends App {
  val inputFilename = args.lift(0).getOrElse("../corpora/interviews/nakapiripirit.tsv")
  val location = "Moroto"
  val tsvReader = new TsvReader()

  Using.resource(Source.fromFile(inputFilename)) { source =>
    val lines = source.getLines
    var count = 0

    lines.zipWithIndex.foreach { case (line, index) =>
      val fields = tsvReader.readln(line)
      val sentence = fields(4)
      val locations = fields(20)

      if (sentence.contains(location)) {
        println(s"$count Instance found on line $index.")
        count += 1

        val hasLocation = locations.contains(location)

        if (!hasLocation)
          println(s"There is a problem with line $index!")
      }
    }
  }
}
