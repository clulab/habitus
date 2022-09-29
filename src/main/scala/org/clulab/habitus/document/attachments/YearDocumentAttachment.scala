package org.clulab.habitus.document.attachments

import org.clulab.processors.Document
import org.clulab.processors.DocumentAttachment
import org.clulab.processors.DocumentAttachmentBuilderFromJson
import org.clulab.processors.DocumentAttachmentBuilderFromText
import org.json4s.JsonDSL._
import org.json4s._

@SerialVersionUID(100L)
class YearDocumentAttachmentBuilderFromText extends DocumentAttachmentBuilderFromText {

  def mkDocumentAttachment(serializedText: String): YearDocumentAttachment = {
    val year = serializedText.toInt
    new YearDocumentAttachment(year)
  }
}

@SerialVersionUID(100L)
class YearDocumentAttachmentBuilderFromJson extends DocumentAttachmentBuilderFromJson {

  def mkDocumentAttachment(yearValue: JValue): YearDocumentAttachment = {
    implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats
    val year = yearValue.extract[Int]
    new YearDocumentAttachment(year)
  }
}

class YearDocumentAttachment(val year: Int) extends DocumentAttachment {

  override def documentAttachmentBuilderFromTextClassName: String = classOf[YearDocumentAttachmentBuilderFromText].getName
  override def documentAttachmentBuilderFromJsonClassName: String = classOf[YearDocumentAttachmentBuilderFromJson].getName

  override def equals(other: Any): Boolean = {
    val that = other.asInstanceOf[YearDocumentAttachment]

    this.year == that.year
  }

  override def toDocumentSerializer: String = year.toString

  override def toJsonSerializer: JValue = JInt(year)
}

object YearDocumentAttachment {
  protected val Key = "year"

  def getYearDocumentAttachment(doc: Document): Option[YearDocumentAttachment] = {
    val documentAttachmentOpt = doc.getAttachment(Key)
    val yearDocumentAttachmentOpt = documentAttachmentOpt.map { documentAttachment =>
      documentAttachment.asInstanceOf[YearDocumentAttachment]
    }

    yearDocumentAttachmentOpt
  }

  def getYear(doc: Document): Option[Int] = {
    val yearDocumentAttachmentOpt = getYearDocumentAttachment(doc)
    val yearOpt = yearDocumentAttachmentOpt.map { yearDocumentAttachment =>
      yearDocumentAttachment.year
    }

    yearOpt
  }

  def setYear(doc: Document, year: Int): YearDocumentAttachment = {
    val yearDocumentAttachment = new YearDocumentAttachment(year)

    doc.addAttachment(Key, yearDocumentAttachment)
    yearDocumentAttachment
  }
}
