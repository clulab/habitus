package org.clulab.habitus.apps.grid

import org.clulab.utils.{FileUtils, Sourcer, StringUtils}
import org.clulab.wm.eidoscommon.utils.{CsvReader, CsvWriter, TsvReader, TsvWriter}
import zamblauskas.csv.parser._
import zamblauskas.functional._

import scala.util.Using

object GridToDatasetKaramojaApp extends App {

  case class GridRecord(row: String, col: String, readable: String)

  case class DatasetRecord(line: String, text: String, row: String)

  implicit val gridDocumentReads: ColumnReads[GridRecord] = (
    column("row").as[String] and
    column("col").as[String] and
    column("readable").as[String]
  )(GridRecord)
  val controlCharacters = " ’✳–\"“”/…½é’€‘—£¬âãé™\u009D"

  val inputDatasetFileName = args.lift(0).getOrElse("../corpora/grid/uq500-only-karamoja/in/uq500-only-karamoja.tsv")
  val gridFileName = args.lift(1).getOrElse("../corpora/grid/uq500-only-karamoja/out/uq500-only-karamoja_cells.csv")
  val ouputDatasetFileName = args.lift(0).getOrElse("../corpora/grid/uq500-only-karamoja/out/uq500-only-karamoja-rowcol.tsv")

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
  val header = Using.resource(Sourcer.sourceFromFilename(inputDatasetFileName)) { source =>
    source.getLines.take(1).toArray.head
  }
  val gridAndDatasetRecordPairs = Using.resource(Sourcer.sourceFromFilename(inputDatasetFileName)) { source =>
    val lines = source.getLines.drop(1)
    val tsvReader = new TsvReader()

    val datasetRecords = lines.flatMap { line =>
      val fields = tsvReader.readln(line)
      val text = fields.lift(7).get
          .filterNot(controlCharacters.contains(_))

      Some(DatasetRecord(line, text, "uq500-only-karamoja"))
    }.toVector
    val gridAndDatasetRecordPairs = datasetRecords.flatMap { datasetRecord =>
      val gridRecordOpt = gridRecords.find { gridRecord =>
        gridRecord.row == datasetRecord.row &&
        datasetRecord.text.contains(gridRecord.readable)
      }

      if (gridRecordOpt.isEmpty) {
        println(datasetRecord.text)
        None
      }
      else Some(gridRecordOpt.get, datasetRecord)
    }

    gridAndDatasetRecordPairs
  }

  Using.resource(FileUtils.printWriterFromFile(ouputDatasetFileName)) { printWriter =>
    printWriter.print(header)
    printWriter.println("\trow\tcol")
    gridAndDatasetRecordPairs.foreach { case (gridRecord, datasetRecord) =>
      printWriter.print(datasetRecord.line)
      printWriter.print("\t" + gridRecord.row)
      printWriter.print("\t" + gridRecord.col)
      printWriter.println()
    }
  }
}
