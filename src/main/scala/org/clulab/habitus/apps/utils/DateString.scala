package org.clulab.habitus.apps.utils

import java.nio.charset.StandardCharsets

class DateString(text: String) {

  def canonicalize: String = DateString.canonicalizeDate(text)
}

object DateString {
  val months = Array("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
  val monthGroup = months.mkString("(", "|", ")")
  val monthMap = months.zipWithIndex.map { case (month, index) => month -> f"${index + 1}%02d" }.toMap

  val        tightRegex = "^(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)T(\\d\\d):(\\d\\d):(\\d\\d)\\+(\\d\\d):(\\d\\d)$$".r // 2022-03-29T14:05:04+00:00
  val  tightShortTRegex = "^(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)T(\\d\\d):(\\d\\d):(\\d\\d)[\\+-](\\d\\d)$$".r // 2022-03-29T14:05:04+00:00
  val       tightZRegex = "^(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)T(\\d\\d):(\\d\\d):(\\d\\d)Z$$".r // 2022-03-29T14:05:04+00:00
  val    tightDateRegex = "^(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)$$".r
  val   looseShortRegex = s"^$monthGroup (\\d\\d?), (\\d\\d\\d\\d)$$".r // August 21, 2016
  val    looseLongRegex = s"^$monthGroup (\\d\\d?), (\\d\\d\\d\\d) (\\d\\d?):(\\d\\d) ([ap]m)$$".r // July 5, 2023 3:26 pm
  val capitalRadioRegex = s"^(\\d\\d) $monthGroup (\\d\\d\\d\\d) - (\\d\\d):(\\d\\d)$$".r
  val        slashRegex = s"^(\\d\\d)/(\\d\\d)/(\\d\\d\\d\\d) (\\d\\d):(\\d\\d):(\\d\\d)$$".r // 12/21/2010 16:16:46

  // This happens for some bad PDFs.
  val badString = new String(Array[Byte](-1, -3, -1, -3), StandardCharsets.UTF_16)

  def apply(text: String): DateString = new DateString(text)

  def canonicalizeDate(date: String): String = {

    date match {
      case "" =>
        date
      case value if value == badString =>
        ""
      case tightRegex(year, month, day, hour, minute, second, timezoneHour, timezoneMinute) =>
        s"${year}-${month}-${day}T${hour}:${minute}:${second}"
      case slashRegex(month, day, year, hour, minute, second) =>
        s"${year}-${month}-${day}T${hour}:${minute}:${second}"
      case tightShortTRegex(year, month, day, hour, minute, second, timezoneHour) =>
        s"${year}-${month}-${day}T${hour}:${minute}:${second}"
      case tightZRegex(year, month, day, hour, minute, second) =>
        s"${year}-${month}-${day}T${hour}:${minute}:${second}"
      case tightDateRegex(year, month, day) =>
        f"${year}-${month}-${day}"
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
      case capitalRadioRegex(day, month, year, hour, minute) =>
        val monthDigits = monthMap(month)

        s"${year}-${monthDigits}-${day}T${hour}:${minute}"
      case string if string.startsWith("-001") => ""
      case _ =>
        throw new RuntimeException(s"$date is unmatched!")
    }
  }
}
