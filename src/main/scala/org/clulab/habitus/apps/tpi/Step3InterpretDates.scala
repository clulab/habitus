package org.clulab.habitus.apps.tpi

import org.clulab.utils.{FileUtils, Logging, Sourcer}
import org.clulab.wm.eidoscommon.utils.TsvReader

import scala.util.Using

object Step3InterpretDates extends App with Logging {
  val inputFileName = "../corpora/uganda/uganda2a.tsv"
  val outputFileName = "../corpora/uganda/uganda2b.tsv"
  val expectedColumnCount = 21

  val months = Array("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
  val monthGroup = months.mkString("(", "|", ")")
  val monthMap = months.zipWithIndex.map { case (month, index) => month -> f"${index + 1}%02d" }.toMap

  val      tightRegex = "^(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)T(\\d\\d):(\\d\\d):(\\d\\d)\\+(\\d\\d):(\\d\\d)$$".r // 2022-03-29T14:05:04+00:00
  val looseShortRegex = s"^$monthGroup (\\d\\d?), (\\d\\d\\d\\d)$$".r // August 21, 2016
  val  looseLongRegex = s"^$monthGroup (\\d\\d?), (\\d\\d\\d\\d) (\\d\\d?):(\\d\\d) ([ap]m)$$".r // July 5, 2023 3:26 pm

  def canonicalizeDate(date: String): String = {
    date match {
      case "" =>
        date
      case tightRegex(year, month, day, hour, minute, second, timezoneHour, timezoneMinute) =>
        val tzHour = "03" // Match local time.
        val zero = "00"

        if (timezoneHour != tzHour)
          println(timezoneHour)
        assert(timezoneHour == tzHour)
        assert(timezoneMinute == zero)
        s"${year}-${month}-${day}T${hour}:${minute}:${second}"
      case looseShortRegex(month, day, year) =>
        val monthDigits = monthMap(month)
        val dayDigits = f"$day%2s".replace(' ', '0')

        f"${year}-${monthDigits}-${dayDigits}"
      case looseLongRegex(month, day, year, hour, minute, amOrPm) =>
        val monthDigits = monthMap(month)
        val dayDigits = f"$day%2s".replace(' ', '0')
        val hour24 = {
          val hour12 = hour.toInt
          assert(hour12 != 0 && hour12 <= 12)

          if (amOrPm == "am") {
            if (hour12 != 12) hour
            else "0"  // 12 am is really 0 am.
          }
          else {
            if (hour12 != 12) (hour12 + 12).toString
            else hour // 12 pm is already pm.
          }
        }
        val hour24Digits = f"$hour24%2s".replace(' ', '0')

        s"${year}-${monthDigits}-${dayDigits}T${hour24Digits}:${minute}"
      case _ =>
        throw new RuntimeException(s"#date is unmatched!")
    }
  }

  Using.resource(Sourcer.sourceFromFilename(inputFileName)) { inputSource =>
    Using.resource(FileUtils.printWriterFromFile(outputFileName)) { printWriter =>
      val tsvReader = new TsvReader()
      val lines = inputSource.getLines
      val firstLine = lines.next

      printWriter.println(s"$firstLine\tcanonicalDate")
      lines.foreach { line =>
        val columnCount = line.count(_ == '\t') + 1
        assert(columnCount == expectedColumnCount)
        val Array(_, _, date) = tsvReader.readln(line, 3)
        val canonicalDate = canonicalizeDate(date)

        println(s"$date -> $canonicalDate")
        printWriter.println(s"$line\t$canonicalDate")
      }
    }
  }
}
