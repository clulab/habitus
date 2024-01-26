package org.clulab.habitus.apps.grid

import org.clulab.utils.{FileUtils, Sourcer, StringUtils}
import zamblauskas.csv.parser._
import zamblauskas.functional._

import scala.util.Using

object GridToLatLongApp extends App {

  // These are the separated but otherwise unprocessed sentences with indexes.
  val gridSentenceFileName = args.lift(0).getOrElse("../corpora/grid/uq500/out/uq500.csv")
  case class GridSentenceRecord(index: Int, sentence: String)
  implicit val gridSentenceRecordReads: ColumnReads[GridSentenceRecord] = (
    column("").as[Int] and
    column("sentence").as[String]
  )(GridSentenceRecord)
  val gridSentenceRecords: Seq[GridSentenceRecord] = {
    val text = FileUtils.getTextFromFile(gridSentenceFileName)
    val parsed = Parser.parse[GridSentenceRecord](text).toOption.get
    val trimmed = parsed.map { gridSentenceRecord =>
      gridSentenceRecord.copy(sentence = gridSentenceRecord.sentence.trim)
    }

    trimmed
  }

  // Each readable will begin with the index into the sentence.
  val gridCellFileName = args.lift(1).getOrElse("../corpora/grid/uq500/out/uq500_cells.csv")
  case class GridCellRecord(row: String, col: String, readable: String)
  case class IndexedGridCellRecord(index: Int, gridCellRecord: GridCellRecord)
  implicit val gridCellRecordReads: ColumnReads[GridCellRecord] = (
    column("row").as[String] and
    column("col").as[String] and
    column("readable").as[String]
  )(GridCellRecord)
  val indexedGridCellRecords: Seq[IndexedGridCellRecord] = {
    val text = FileUtils.getTextFromFile(gridCellFileName)
    val parsed = Parser.parse[GridCellRecord](text)
    val allGridCellRecords = parsed.toOption.get
    val gridCellRecords = allGridCellRecords.filter { gridCellRecord =>
      gridCellRecord.row != "all"
    }
    val indexedGridCellRecords = gridCellRecords.map { gridCellRecord =>
      val index = StringUtils.beforeFirst(gridCellRecord.readable, '.').toInt

      IndexedGridCellRecord(index, gridCellRecord)
    }

    indexedGridCellRecords
  }

  case class GridRecord(sentence: GridSentenceRecord, cell: IndexedGridCellRecord)
  val gridRecords = gridSentenceRecords.map { gridSentenceRecord =>
    val indexedGridCellRecord = indexedGridCellRecords.find { indexedGridCellRecord =>
      indexedGridCellRecord.index == gridSentenceRecord.index
    }.get

    GridRecord(gridSentenceRecord, indexedGridCellRecord)
  }

  val inputDatasetFileName = args.lift(1).getOrElse("../corpora/grid/uq500/in/uq500_sentences.csv")
  case class InputDatasetRecord(sentence: String, lat: Float, lon: Float, geometry: String)
  implicit val datasetRecordReads: ColumnReads[InputDatasetRecord] = (
    column("sentence").as[String] and
    column("lat").as[Float] and
    column("lon").as[Float] and
    column("geometry").as[String]
  )(InputDatasetRecord)
  val inputDatasetRecords: Seq[InputDatasetRecord] = {
    val text = FileUtils.getTextFromFile(inputDatasetFileName)
    val parsed = Parser.parse[InputDatasetRecord](text)
    val inputDatasetRecords = parsed.toOption.get

    inputDatasetRecords
  }
  val inputDatasetLines = Using.resource(Sourcer.sourceFromFilename(inputDatasetFileName)) { source =>
    source.getLines.drop(1).toVector
  }
  case class InputDatasetRecordAndLine(record: InputDatasetRecord, line: String)
  val inputDatasetRecordAndLineSeq = inputDatasetRecords.zip(inputDatasetLines).map { case (inputDataSetRecord, inputDatasetLine) =>
    InputDatasetRecordAndLine(inputDataSetRecord, inputDatasetLine)
  }

  case class OutputDatasetRecord(input: InputDatasetRecordAndLine, gridRecord: GridRecord)
  var skipOutputCount = 0
  val outputDatasetRecords: Seq[OutputDatasetRecord] = inputDatasetRecordAndLineSeq.zipWithIndex.map { case (inputDatasetRecordAndLine, index) =>
    val inputSentence = inputDatasetRecordAndLine.record.sentence.filterNot(_ == ' ')

    while ({
      val gridSentence = gridRecords(index + skipOutputCount).sentence.sentence.filterNot(_ == ' ')
      val matches = inputSentence.contains(gridSentence) || gridSentence.contains(inputSentence)

      if (!matches) {
        println(gridSentence)
        println(inputSentence)
      }
      !matches
    })
      skipOutputCount += 1

    val result = OutputDatasetRecord(inputDatasetRecordAndLine, gridRecords(index + skipOutputCount))

    {
      val gridSentence = gridRecords(index + skipOutputCount).sentence.sentence.filterNot(_ == ' ')

      if (gridSentence.length > inputSentence.length && gridSentence.startsWith(inputSentence)) {
        // The entire gridSentence has not yet been used up, so continue with it.
        // The next time around will probably have gridSentence.endsWith(inputSentence).
        skipOutputCount -= 1
      }
    }

    result
  }

  val ouputDatasetFileName = args.lift(3).getOrElse("../corpora/grid/uq500/out/uq500_sentences_rowcol.csv")
  Using.resource(FileUtils.printWriterFromFile(ouputDatasetFileName)) { printWriter =>
    printWriter.println("sentence,lat,lon,geometry,row,col")

    outputDatasetRecords.foreach { outputDatasetRecord =>
      printWriter.print(outputDatasetRecord.input.line) // output exactly as input
      printWriter.print("," + outputDatasetRecord.gridRecord.cell.gridCellRecord.row)
      printWriter.print("," + outputDatasetRecord.gridRecord.cell.gridCellRecord.col)
      printWriter.println()
    }
  }
}
