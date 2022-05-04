package org.clulab.habitus.variables

import org.clulab.habitus.utils.Test
import org.clulab.odin.{Mention, TextBoundMention}

class TestVariableReaderLabelBased extends Test {
  // checks Assignment mentions:
  // the variable argument should be a text bound mention of the correct label
  // the value argument should have a correct norm
  val vp: VariableProcessor = VariableProcessor()

  // (variableLabel, Seq[(valueText, valueNorm)])
  // So if one mention has multiple values, write Seq((valueText1, valueNorm1), (valueText2, valueNorm2), ...)
  type Variable = (String, Seq[(String, String)])

  case class VariableTest(
     name: String, text: String,
     // If there are multiple "Assignment" mentions, use one variable for each and multiple lines.
     label: String,
     variables: Seq[Variable]
   ) {

    def getMentions(text: String): Seq[Mention] = {
      val parsingResults = vp.parse(text)
      parsingResults.allMentions
    }

    def test(index: Int): Unit = {
      it should s"process $index-$name correctly" in {
        if (index == -1)
          println("Put a breakpoint here to observe a particular test.")

        val allMentions = getMentions(text)
        val mentions = allMentions.filter(_.label matches label).sortBy(_.tokenInterval)

        if (variables.isEmpty) {
          // get only relations and events
          val nonTextBoundMentions = mentions.filter(m => !m.isInstanceOf[TextBoundMention])
          // there should be none
          nonTextBoundMentions.length should be (0)
        } else {

          mentions should have size variables.length

          variables.zip(mentions).zipWithIndex.foreach { case ((variable, mention), variableIndex) =>
            (variableIndex, mention.arguments("variable").head.label) should be((variableIndex, variable._1))

            val values = variable._2
            val arguments = mention.arguments("value")

            arguments should have size values.length
            values.zip(arguments).zipWithIndex.foreach { case ((value, argument), valueIndex) =>
              // not checking text right now, just the norm
              //            (variableIndex, valueIndex, argument.text) should be ((variableIndex, valueIndex, value._1))
              (variableIndex, valueIndex, argument.norms.get.head) should be((variableIndex, valueIndex, value._2))
            }
          }
        }
      }
    }
  }

  behavior of "VariableReader"

  val variableTests: Array[VariableTest] = Array(
    VariableTest(
      "sent1", "with an average yield over years and seasons of 5 t ha-1",
      "YieldAmount",
      Seq(
        ("Yield", Seq(("5 t ha-1", "5.0 t/ha")))
      )
    ),
    VariableTest(
      "negTestSent", "This is a sample negative test. There should be no assignments extracted",
      "None",
      Seq.empty
    )
  )

  variableTests.zipWithIndex.foreach { case (variableTest, index) => variableTest.test(index) }
}
