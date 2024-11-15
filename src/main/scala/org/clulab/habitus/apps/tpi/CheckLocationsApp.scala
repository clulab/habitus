package org.clulab.habitus.apps.tpi

import org.clulab.habitus.utils.TsvReader
import org.clulab.utils.Sourcer

import scala.util.Using

object CheckLocationsApp extends App {
//  val locationsFileName = "./belief_pipeline/GH.tsv"
//  val locationsFileName = "./belief_pipeline/SN.tsv"
  val locationsFileName = "./belief_pipeline/UG.tsv"
  val tsvReader = new TsvReader()

  val nameAndAllNamesSeq = Using.resource(Sourcer.sourceFromFilename(locationsFileName)) { source =>
    val nameToAllNames = source.getLines.map { line =>
      val Array(_, name, asciiName, alternateNames) = tsvReader.readln(line, 4)
      val alternates = alternateNames.split(',')
      val allNames = alternates :+ asciiName :+ name

      name -> allNames.filter(_.nonEmpty).toSet
    }.toVector

    nameToAllNames
  }

  nameAndAllNamesSeq.zipWithIndex.foreach { case (outerNameAndAllNames, outerIndex) =>
    nameAndAllNamesSeq.zipWithIndex.foreach { case (innerNameAndAllNames, innerIndex) =>
      if (outerIndex != innerIndex) {
        // There should be no overlap between the two sets.
        val intersection = outerNameAndAllNames._2.intersect(innerNameAndAllNames._2)
        // We allow those named exactly the same to be ambiguous.
        val condition = intersection.nonEmpty && outerNameAndAllNames._1 != innerNameAndAllNames._1

        if (condition) {
          val outerName = outerNameAndAllNames._1
          val innerName = innerNameAndAllNames._1
          val intersectingNames = intersection.mkString(", ")

          println(s"${outerIndex + 1} $outerName ${innerIndex + 1} $innerName => $intersectingNames")
        }
      }
    }
  }
}
