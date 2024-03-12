package org.clulab.habitus.apps.grid

import org.clulab.utils.{FileUtils, Sourcer, StringUtils}
import org.clulab.wm.eidoscommon.utils.{CsvReader, CsvWriter, TsvReader, TsvWriter}
import zamblauskas.csv.parser._
import zamblauskas.functional._

import scala.util.Using

case class GridRecord(row: String, col: String, readable: String)

case class DatasetRecord(line: String, text: String, row: String)

object GridToDatasetApp extends App {
  implicit val gridDocumentReads: ColumnReads[GridRecord] = (
    column("row").as[String] and
    column("col").as[String] and
    column("readable").as[String]
  )(GridRecord)
  val controlCharacters = " ’✳–\"“”/…½é’€‘—£¬âãé™\u009D"

  val inputDatasetFileName = args.lift(0).getOrElse("../corpora/grid/uq500-karamoja/in/uq500-karamoja.tsv")
  val gridFileName = args.lift(1).getOrElse("../corpora/grid/uq500-karamoja/out/uq500-karamoja_zip_cells.csv")
  val ouputDatasetFileName = args.lift(0).getOrElse("../corpora/grid/uq500-karamoja/out/uganda-uq500-karamoja-rowcol.tsv")

  val gridRecords: Seq[GridRecord] = {
    val text = FileUtils.getTextFromFile(gridFileName)
    val result = Parser.parse[GridRecord](text)
    val rawGridRecords = result.toOption.get
    val documents = rawGridRecords.map { gridRecord =>
      val text = StringUtils.afterFirst(gridRecord.readable, '.').drop(1)
        .filterNot(controlCharacters.contains(_))

      gridRecord.copy(readable = text)
    }
    val rowDocuments = documents.filter { gridRecord =>
      gridRecord.row != "all"
    }

    rowDocuments
  }
  val gridAndDatasetRecordPairs = Using.resource(Sourcer.sourceFromFilename(inputDatasetFileName)) { source =>
    val lines = source.getLines.drop(1)
    val tsvReader = new TsvReader()

    val datasetRecords = lines.flatMap { line =>
      val fields = tsvReader.readln(line)
      val text = fields.lift(4).get
          .filterNot(controlCharacters.contains(_))
      val dateOpt = {
        val date = fields(21)

        if (date.nonEmpty) Some(date.take(4))
        else None
      }
      val locationOpt = {
        val location = fields(19)

        // Use them only if they are a simple location.
        if (location.count(_ == ',') == 1) Some(StringUtils.beforeFirst(location, ' ', true))
        else None
      }

      // if (locationOpt.isDefined) Some(DatasetRecord(line, text, "uganda-" + dateOpt.get)) else None
      // if (locationOpt.isDefined) Some(DatasetRecord(line, text, "uganda-" + locationOpt.get)) else None
      // For these last ones there was not necessarily a location.
      Some(DatasetRecord(line, text, "uq500-karamoja"))
    }.toVector
    val gridAndDatasetRecordPairs = gridRecords.flatMap { gridRecord =>
      val datasetRecordOpt = datasetRecords.find { datasetRecord =>
        gridRecord.row == datasetRecord.row &&
        datasetRecord.text.contains(gridRecord.readable)
      }

      if (datasetRecordOpt.isEmpty) {
        println(gridRecord.readable)
        None
      }
      else Some(gridRecord, datasetRecordOpt.get)
    }

    gridAndDatasetRecordPairs
  }

  Using.resource(FileUtils.printWriterFromFile(ouputDatasetFileName)) { printWriter =>
    printWriter.println("url\tterms\tdate\tsentenceIndex\tsentence\tcausal\tcausalIndex\tnegationCount\tcauseIncCount\tcauseDecCount\tcausePosCount\tcauseNegCount\teffectIncCount\teffectDecCount\teffectPosCount\teffectNegCount\tcauseText\teffectText\tbelief\tsent_locs\tcontext_locs\tcanonicalDate\tprevLocation\tprevDistance\tnextLocation\tnextDistance\trow\tcol")
    gridAndDatasetRecordPairs.foreach { case (gridRecord, datasetRecord) =>
      printWriter.print(datasetRecord.line)
      printWriter.print("\t" + gridRecord.row)
      printWriter.print("\t" + gridRecord.col)
      printWriter.println()
    }
  }
}
