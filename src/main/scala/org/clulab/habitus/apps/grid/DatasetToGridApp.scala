package org.clulab.habitus.apps.grid

import org.clulab.habitus.utils.{CsvWriter, TsvReader, TsvWriter}
import org.clulab.utils.{FileUtils, Sourcer, StringUtils}

import scala.util.{Random, Using}

case class Record(text: String, date: String, location: String)

object DatasetToGridApp extends App {
  val datasetFileName = args.lift(0).getOrElse("../corpora/grid/uq500-karamoja/in/uq500-karamoja.tsv")
  val gridFileNamePrefix = args.lift(1).getOrElse("../corpora/grid/uganda-")
  val gridFileNameSuffix = args.lift(1).getOrElse(".txt")
  val random = new Random(0)

  val (dateMap, locationMap) = Using.resource(Sourcer.sourceFromFilename(datasetFileName)) { source =>
    val tsvReader = new TsvReader()
    val lines = source.getLines.drop(1)
    val records = lines.flatMap { line =>
      val fields = tsvReader.readln(line)
      val text = fields.lift(4).get
      val dateOpt = {
        val date = fields(21)

        if (date.nonEmpty) Some(date.take(4))
        else None
      }
      val locationOpt = {
        val location = fields(19)

        // Use them only if they are a simple location.
        if (location.count(_ == ',') == 1) Some(location)
        else None
      }

      // Use only whole sentences that end in a period or else the grid combine lines.
      if (locationOpt.isDefined && dateOpt.isDefined && text.endsWith("."))
        Some(Record(text, dateOpt.get, locationOpt.get))
      else
        None
    }.toVector
    val dateMap = {
      val dateGroups = records.groupBy(_.date)
      val dateKeys = dateGroups.keys.toVector.sorted.reverse.take(5)
      val dateMap = dateKeys.map { key => key -> random.shuffle(dateGroups(key)).take(100) }

      dateMap.toMap
    }
    val locationMap = {
      val locationGroups = records.groupBy(_.location)
      val locationKeyValues = locationGroups.toVector.sortBy(-_._2.length).take(5)
      val locationMap = locationKeyValues.map { case (key, values) =>
        key -> random.shuffle(values).take(100)
      }

      locationMap.toMap
    }

    (dateMap, locationMap)
  }

  Seq(dateMap, locationMap). foreach { map =>
    map.foreach { case (key, records) =>
      val shortKey = StringUtils.beforeFirst(key, ' ', all = true)
      val fileName = s"$gridFileNamePrefix$shortKey$gridFileNameSuffix"

      Using.resource(FileUtils.printWriterFromFile(fileName)) { printWriter =>
        records.foreach { record =>
          printWriter.println(record.text)
        }
      }
    }
  }
}
