package org.clulab.habitus.variables

import org.clulab.habitus.utils.{DefaultContext, Test}
import org.clulab.odin.Mention

class TestDefaultContextAttachment extends Test {
  // make sure a relation/event attachment is correct
  // each test is supposed to have only one target mention extracted
  val vp: VariableProcessor = VariableProcessor()

  val LOCATION = "location"
  val DATE = "date"
  val PROCESS = "process"
  val CROP = "crop"
  val FERTILIZER = "fertilizer"
  val COMPARATIVE = "comparative"
  val NA = "N/A"

  case class DefaultContextAttachmentTest(
                           name: String,
                           // Text for extraction
                           text: String,
                           label: String,
                           desired: Map[String, String]
                           // Expected attachment fields and values
                         ) {

    def getMentions(text: String): Seq[Mention] = {
      val parsingResults = vp.parse(text)
      // in this test, we only need the relation/event mentions, not text bound mentions
      parsingResults.targetMentions
    }


    def test(index: Int): Unit = {
      it should s"have a correct attachment in $name" in {
        if (index == -1)
          println("Put a breakpoint here to observe a particular test.")

        // get event/relation mentions from text
        val relevantMentions = getMentions(text).filter(_.label == label)
        // there should be one target mention
        // (relation or event) extracted
        relevantMentions.length should be (1)
        val attachments = relevantMentions.head.attachments
        // there should be one attachment per target mention
        attachments.toList.length should be (1)
        val attachment = attachments.head.asInstanceOf[DefaultContext]
        attachment.location should be (desired("location"))
        attachment.date should equal (desired("date"))
        attachment.process should equal (desired("process"))
        attachment.crop should equal (desired("crop"))
        attachment.fertilizer should equal (desired("fertilizer"))
        attachment.comparative.toString should equal (desired("comparative"))
      }
    }
  }

  behavior of "VariableReader Entities"

  val variableTests: Array[DefaultContextAttachmentTest] = Array(
    DefaultContextAttachmentTest(
      "sent-1",
      "The area sown with rice and fertilized with urea for this 2021/2022 wintering campaign is 28,223 ha in wintering in Senegal.",
      "PlantingArea",
      Map(
        LOCATION -> "Senegal",
        DATE -> "2021/2022",
        PROCESS -> "planting",
        CROP -> "rice",
        FERTILIZER -> "urea",
        COMPARATIVE -> "0"
      )
    ),
      DefaultContextAttachmentTest(
        // tests
        // 1) that if there are no locations within the sentence, pick the most frequent one occurring in +/- 2 sentence context window
        // 2) absent contexts are stored as N/A
      "sent-2",
      "This is a sentence about Senegal. The area sown for this 2021/2022 wintering campaign is 28,223 ha in wintering. All of this happened in Senegal, not the U.S.",
      "PlantingArea",
      Map(
        LOCATION -> "Senegal",
        DATE -> "2021/2022",
        PROCESS -> "planting",
        CROP -> NA,
        FERTILIZER -> NA,
        COMPARATIVE -> "0"
      )
    ),
    DefaultContextAttachmentTest(
      // tests
      // 1) that if there are multiple locations within the sentence, pick the nearest one
      // 2) comparative is present
      "sent-3",
      "The areas sown for this 2021/2022 wintering campaign are 28,223 ha in Senegal vs 35,065 ha in the U.S.",
      "PlantingArea",
      Map(
        LOCATION -> "Senegal",
        DATE -> "2021/2022",
        PROCESS -> "planting",
        CROP -> NA,
        FERTILIZER -> NA,
        COMPARATIVE -> "1"
      )
    )
  )

  variableTests.zipWithIndex.foreach { case (variableTest, index) => variableTest.test(index) }
}


