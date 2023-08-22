package org.clulab.habitus.apps.tpi

import org.clulab.utils.{Logging, Sourcer}
import org.clulab.wm.eidoscommon.utils.TsvReader

import java.util.regex.Pattern
import scala.util.Using

object Step3InterpretDates extends App with Logging {
  val inputFileName = "../corpora/multi/CausalBeliefs.tsv"
  val outputFileName = "../corpora/multi/CausalBeliefsDate.tsv"

  val patterns = Seq(
    "\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d\\+\\d\\d:\\d\\d", // 2022-03-29T14:05:04+00:00
    "(July|August) \\d\\d?, \\d\\d\\d\\d", // August 21, 2016
    "(February|July|November) \\d\\d?, \\d\\d\\d\\d \\d\\d?:\\d\\d [ap]m", // July 5, 2023 3:26 pm
    "\\d\\d?-\\d\\d?-\\d\\d\\d\\d  \\d\\d?:\\d\\d:\\d\\d [AP]M"  //
  )

  def convertDate(date: String): String = {

    if (!patterns.exists(Pattern.matches(_,date)))
      print(s"$date is unmatched!")

//    if (Pattern.matches(pattern1, date))
//      println("pattern1!") // OK
//    if (Pattern.matches(pattern2, date))
//      println("pattern2!")

    //    date match {
//      case pattern1(_) =>
//        "long"
//
//      case _ => date
//    }
    date
  }

  Using.resource(Sourcer.sourceFromFilename(inputFileName)) { inputSource =>
    val tsvReader = new TsvReader()

    inputSource.getLines.foreach { line =>
      val Array(_, _, date) = tsvReader.readln(line, 3)
        println(s"$date -> ${convertDate(date)}")
    }
  }
}
