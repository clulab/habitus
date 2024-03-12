package org.clulab.habitus.apps.grid

import org.clulab.utils.{FileUtils, Sourcer}

import scala.util.Using


// Change all commas outside of quoted strings to tabs.
// Remove double quotes.
// Replace some special characters like tab itself.
object Csv2Tsv extends App {
  val csvFilename = args.lift(0).getOrElse("../corpora/grid/uq500-karamoja/in/uq500-karamoja.csv")
  val tsvFilename = args.lift(1).getOrElse("../corpora/grid/uq500-karamoja/in/uq500-karamoja.tsv")

  trait State
  object OutsideFieldState extends State
  object InsideFieldState extends State
  object InsideQuotedFieldState extends State
  object InsideQuotedQuoteState extends State

  Using.resource(Sourcer.sourceFromFilename(csvFilename)) { source =>
    val lines = source.getLines

    Using.resource(FileUtils.printWriterFromFile(tsvFilename)) { printWriter =>

      def outputString(string: String): Unit = printWriter.print(string)

      def outputChar(char: Char): Unit = printWriter.print(char)

      def outputNewline(): Unit = printWriter.println

      def throwChar(char: Char, state: State): Nothing = {
        throw new RuntimeException(s"Char '$char' is invalid in state $state.")
      }

      val nextState = lines.foldLeft(OutsideFieldState: State) { (state, line) =>
        val nextState = line.foldLeft(state: State) { (state, char) =>
          val nextState = state match {
            case OutsideFieldState =>
              // I should only see a quote or some char that starts the field.
              val nextState = char match {
                case '\n' => throwChar(char, state)
                case '\r' => throwChar(char, state)
                case '\t' => throwChar(char, state)
                case ','  => throwChar(char, state)
                case '"'  => InsideQuotedFieldState
                case '\\' => throwChar(char, state)
                case _    => outputChar(char); InsideFieldState
              }
              nextState
            case InsideFieldState =>
              // I need to escape special characters and watch for the looming comma.
              val nextState = char match {
                case '\n' => outputString("\\n"); InsideFieldState
                case '\r' => outputString("\\r"); InsideFieldState
                case '\t' => outputString("\\t"); InsideFieldState
                case ','  => outputChar('\t'); OutsideFieldState
                case '"'  => throwChar(char, state)
                case '\\' => outputString("\\\\"); InsideFieldState
                case _    => outputChar(char); InsideFieldState
              }
              nextState
            case InsideQuotedFieldState =>
              // I need to escape special characters and watch for the looming end quote or doubled false alarms.
              val nextState = char match {
                case '\n' => outputString("\\n"); InsideQuotedFieldState
                case '\r' => outputString("\\r"); InsideQuotedFieldState
                case '\t' => outputString("\\t"); InsideQuotedFieldState
                case ','  => outputChar(char); InsideQuotedFieldState
                case '"'  => InsideQuotedQuoteState
                case '\\' => outputString("\\\\"); InsideQuotedFieldState
                case _    => outputChar(char); InsideQuotedFieldState
              }
              nextState
            case InsideQuotedQuoteState =>
              // I just saw a quote while InsideQuotedFieldState and need to decide what to do.
              val nextState = char match {
                case '\n' => throwChar(char, state)
                case '\r' => throwChar(char, state)
                case '\t' => throwChar(char, state)
                case ','  => outputChar('\t'); OutsideFieldState // We are now outside the field.
                case '"'  => outputChar('"'); InsideQuotedFieldState // It was a double quote and we're still inside the field.
                case '\\' => throwChar(char, state)
                case _    => throwChar(char, state)
              }
              nextState
          }
          nextState
        }
        // The line is finished.
        nextState match {
          case OutsideFieldState => outputNewline(); OutsideFieldState
          case InsideFieldState => outputNewline(); OutsideFieldState
          case InsideQuotedFieldState => outputString("\\n"); InsideQuotedFieldState
          case InsideQuotedQuoteState => outputNewline(); OutsideFieldState
        }
        nextState
      }
      // The file is finished.
      nextState match {
        case OutsideFieldState => assert(true)
        case InsideFieldState => assert(false)
        case InsideQuotedFieldState => throw new RuntimeException("Quoted field was not terminated correctly.")
        case InsideQuotedQuoteState => assert(false)
      }
    }
  }
}
