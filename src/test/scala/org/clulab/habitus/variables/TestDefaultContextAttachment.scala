package org.clulab.habitus.variables

import org.clulab.habitus.utils.{DefaultContext, Test}
import org.clulab.odin.Mention
import scala.collection.immutable.ListMap

class TestDefaultContextAttachment extends Test {
  // make sure a relation/event attachment is correct
  // each test is supposed to have only one target mention extracted
  val vp: VariableProcessor = VariableProcessor()

  val NA = "N/A"

  case class Desired(mentionText: String, location: String = NA, date: String = NA, process: String = NA, crop: String = NA, fertilizer: String = NA, comparative: String = NA, season: String = NA)

  case class DefaultContextAttachmentTest(
                           shouldable: Shouldable,
                           name: String,
                           // Text for extraction
                           text: String,
                           label: String,
                           desireds: Seq[Desired]
                           // Expected attachment fields and values
                         ) {

    def getMentions(text: String): Seq[Mention] = {
      val parsingResults = vp.parse(text)
      // in this test, we only need the relation/event mentions, not text bound mentions
      parsingResults.targetMentions
    }


    def test(index: Int): Unit = {
      shouldable should s"have a correct attachment in $name" in {
        if (index == -1)
          println("Put a breakpoint here to observe a particular test.")

        // get event/relation mentions from text
        val relevantMentions = getMentions(text).filter(_.label == label).sortBy(_.tokenInterval)
        // there should be one target mention
        // (relation or event) extracted
        relevantMentions.length should equal(desireds.length)
        for ((rm, idx) <- relevantMentions.zipWithIndex)  {
          val desired = desireds(idx)
          rm.text should equal(desired.mentionText)
          val attachments = rm.attachments
          // there should be one attachment per target mention
          attachments.toList.length should be (1)
          val attachment = attachments.head.asInstanceOf[DefaultContext]
          attachment.location should be (desired.location)
          attachment.date should equal (desired.date)
          attachment.process should equal (desired.process)
          attachment.crop should equal (desired.crop)
          attachment.fertilizer should equal (desired.fertilizer)
          attachment.comparative.toString should equal (desired.comparative)
        }

      }
    }
  }

  behavior of "VariableReader Entities"

  val variableTests: Array[DefaultContextAttachmentTest] = Array(
    DefaultContextAttachmentTest(
      passingTest,
      "sent-1",
      "The area sown with rice and fertilized with urea for this 2021/2022 wintering campaign is 28,223 ha in wintering in Senegal.",
      "AreaSize",
      Seq(
        Desired(
          mentionText = "area sown with rice and fertilized with urea for this 2021/2022 wintering campaign is 28,223 ha",
          location = "Senegal",
          date = "2021/2022",
          process = "planting",
          crop = "rice",
          fertilizer =  "urea",
          comparative =  "0"
        )
      )
    ),
      DefaultContextAttachmentTest(
        // tests
        // 1) that if there are no locations within the sentence, pick the most frequent one occurring in +/- 2 sentence context window
        // 2) absent contexts are stored as N/A
        passingTest,
      "sent-2",
      "This is a sentence about Senegal. The area sown for this 2021/2022 wintering campaign is 28,223 ha in wintering. All of this happened in Senegal, not the U.S.",
      "AreaSize",
      Seq(
        Desired(
          mentionText = "area sown for this 2021/2022 wintering campaign is 28,223 ha",
          location = "Senegal",
          date = "2021/2022",
          process = "planting",
          comparative = "0"
        )
      )
    ),
    DefaultContextAttachmentTest(
      // tests
      // 1) that if there are multiple locations within the sentence, pick the nearest one
      // 2) comparative is present
      passingTest,
      "sent-3",
      "The areas sown for this 2021/2022 wintering campaign are 28,223 ha in Senegal vs 35,065 ha in the U.S.",
      "AreaSize",
      Seq(
        Desired(
          mentionText = "areas sown for this 2021/2022 wintering campaign are 28,223 ha",
          location = "Senegal",
          date = "2021/2022",
          process = "planting",
          comparative = "1"
        )
      )
    ),
    DefaultContextAttachmentTest(
      // FIXME: irrigation should win here; need more sophisticated procedure for picking process
      failingTest,
      "sent-4",
      "This type of irrigation scheme ( or perimeter ) , with an area of below 50 ha and cultivated by farmers from a single village , covers about 25 % of the irrigated area on the two banks of the Senegal River ( SAED , 1997 ; SONADER , 1998 ) .",
      "AreaSize",
      Seq(
        Desired(
          mentionText = "area of below 50 ha",
          process = "irrigation"
        ),
        Desired(
          mentionText = "25 % of the irrigated area",
          process = "irrigation"
        )
      )
    ),
    DefaultContextAttachmentTest(
      passingTest,
      "sent-5",
      "In the 2000WS, they used cultivar Sahel 202",
      "CropAssignment",
      Seq(
        Desired(
          mentionText = "cultivar Sahel 202",
          date = "2000WS",
          crop = "Sahel 202",
          comparative = "0",
          process = "planting"
        )
      )
    ),
    DefaultContextAttachmentTest(
      passingTest,
      "sent-6",
      "We want to make a paragraph that has more than six sentences to test how a document level entity filter works. This is the second sentence. The next sentence contains the target mention. At PI , high levels of weed infestation ( weed cover / 10 % of the area ) was observed in 33 out the 50 direct-seeded plots and only 11 out the 37 transplanted plots. We want the acronym for `panicle initiation` to not be extracted as a location. This is sentence six. And finally, sentence seven.",
      "AreaSize",
      Seq(
        Desired(
          mentionText = "10 % of the area",
          process = "weeds",
          comparative = "0"
        )
      )
    ),
    DefaultContextAttachmentTest(
      passingTest,
      "sent-7",
      "Average yield reached 7.2 t ha-1 in 1999 and 8.2 t ha-1 in 2000 , without any significant increase in production costs .",
      "YieldAmount",
      Seq(
        Desired(
          mentionText = "yield reached 7.2 t ha-1",
          date = "1999",
          process = "harvesting",
          comparative = "0"
        ),
        Desired(
          mentionText = "yield reached 7.2 t ha-1 in 1999 and 8.2 t ha-1",
          date = "2000",
          process = "harvesting",
          comparative = "0"
        )
      )
    ),
    DefaultContextAttachmentTest(
      passingTest,
      "sent-8",
      "Specifically , in the dry season of 2016 , Caritas funded the cultivation of 30 ha corresponding to seven GIEs ; whereas , in 2017 , the overall area managed by the NGO was 15 ha .",
      "AreaSize",
      Seq(
        Desired(
          mentionText = "30 ha corresponding to seven GIEs ; whereas , in 2017 , the overall area",
          date = "2016",
          process = "planting",
          comparative = "0"
        ),
        Desired(
          mentionText = "area managed by the NGO was 15 ha",
          date = "2017",
          process = "planting",
          comparative = "0"
        )
      )
    ),
    DefaultContextAttachmentTest(
      passingTest,
      "sent-9",
      "and average of potential yield calculated with the model ORYZAS was 8.7 t ha-1 for Jaya and 8.2 t ha-1 for Sahel 108 .",
      "YieldAmount",
      Seq(
        Desired(
          mentionText = "yield calculated with the model ORYZAS was 8.7 t ha-1",
          crop = "Jaya",
          process = "harvesting",
          comparative = "0"
        ),
        Desired(
          mentionText = "yield calculated with the model ORYZAS was 8.7 t ha-1 for Jaya and 8.2 t ha-1",
          crop = "Sahel 108",
          process = "harvesting",
          comparative = "0"
        )
      )
    ),
    DefaultContextAttachmentTest(
      passingTest,
      "sent-10",
      "Popular varieties in the wet season were Sahel 202 ( 65 % of farmers ) and Sahel 201 ( 30 % ) , while 60 % and 92 % grew Sahel-108 in 2012DS and 2013DS , respectively.",
      "CropAssignment",
      Seq(
        Desired(
          mentionText = "varieties in the wet season were Sahel 202",
          crop = "Sahel 202",
          date = "2012DS", // fixme: unclear if this is the best date for this sentence
          process = "planting",
          comparative = "1"
        ),
        Desired(
          mentionText = "grew Sahel-108",
          crop = "Sahel-108",
          date = "2012DS",
          process = "planting",
          comparative = "1"
        )
      )
    ),
    DefaultContextAttachmentTest(
      passingTest,
      "sent-11",
      "Popular varieties in the wet season were Sahel 202 ( 65 % of farmers ) and Sahel 201 ( 30 % ) , while 60 % and 92 % grew Sahel-108 in 2012DS and 2013DS , respectively.",
      "PlantingEvent",
      Seq(
        Desired(
          mentionText = "grew Sahel-108",
          crop = "Sahel-108",
          date = "2012DS",
          process = "planting",
          comparative = "1"
        )
      )
    ),
    DefaultContextAttachmentTest(
      passingTest,
      "sent-12",
      " Plots with higher K2O fertilizer showed small yield advantage ( on average 0.3 t / ha ) , although there was no statistical difference in yield between the K2O rates .",
      "YieldAmount",
      Seq(
        Desired(
          mentionText = "yield advantage ( on average 0.3 t / ha",
          fertilizer = "K2O",
          process = "harvesting",
          comparative = "0"
        )
      )
    ),
    DefaultContextAttachmentTest(
      passingTest,
      "sent-13",
      "P and K concentrations in irrigation and floodwater were estimated at 0.1 mg P l-1 and 3.2 mg K l-1 .",
      "FertilizerQuantity",
      Seq(
        Desired(
          mentionText = "0.1 mg P l-1",
          fertilizer = "P",
          process = "fertilizerApplication",
          comparative = "0"
        ),
        Desired(
          mentionText = "3.2 mg K l-1",
          fertilizer = "K",
          process = "fertilizerApplication",
          comparative = "0"
        )
      )
    ),
    DefaultContextAttachmentTest(
      failingTest,
      "sent-15",
      "Average rice yield is generally high in both wet and dry seasons at 5.4 and 6.6 t / ha , respectively ; however , there is large variation among farmers ( e.g .",
      "YieldAmount",
      Seq(
        Desired(
          mentionText = "yield is generally high in both wet and dry seasons at 5.4 and 6.6 t / ha",
          season = "dry season",
          comparative = "0"
        ),
        Desired(
          mentionText = "yield is generally high in both wet and dry seasons at 5.4",
          season = "wet season",
          comparative = "0"
        )
      )
    ),
    DefaultContextAttachmentTest(
      passingTest,
      "sent-16",
      "In the 1998WS, farmers sowed Jaya between 20 June and 1 July ( Tab .",
      "WetSeasonAssignment",
      Seq(
        Desired(
        mentionText = "1998WS, farmers sowed Jaya between 20 June and 1 July",
        date = "between 20 June and 1 July",
        process = "planting",
        crop = "Jaya",
        season = "1998WS",
        comparative = "0"
        )
      )
    )
    ,
    DefaultContextAttachmentTest(
      passingTest,
      "sent-17",
      "In the 1998WS, farmers sowed Jaya between 20 June and 1 July ( Tab .",
      "PlantingEvent",
      Seq(
        Desired(
          mentionText = "sowed Jaya between 20 June and 1 July",
          date = "1998WS",
          process = "planting",
          crop = "Jaya",
          season = "1998WS",
          comparative = "0"
        )
      )
    )
  )

  variableTests.zipWithIndex.foreach { case (variableTest, index) => variableTest.test(index) }
}


