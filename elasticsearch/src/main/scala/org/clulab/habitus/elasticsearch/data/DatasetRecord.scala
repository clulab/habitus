package org.clulab.habitus.elasticsearch.data

import java.lang.{Boolean => JBoolean, Integer => JInteger, Float => JFloat}
import java.util.HashMap

case class CauseOrEffect(
  text: String,
  incCount: Int,
  decCount: Int,
  posCount: Int,
  negCount: Int
) {

  def serialize(): HashMap[String, AnyRef] = {
    val map = new HashMap[String, AnyRef]()

    map.put("text", text)
    map.put("incCount", incCount.asInstanceOf[JInteger])
    map.put("decCount", decCount.asInstanceOf[JInteger])
    map.put("posCount", posCount.asInstanceOf[JInteger])
    map.put("negCount", negCount.asInstanceOf[JInteger])
    map
  }
}

case class Relation(cause: CauseOrEffect, effect: CauseOrEffect) {

  def serialize(): HashMap[String, AnyRef] = {
    val map = new HashMap[String, AnyRef]()

    map.put("cause", cause.serialize())
    map.put("effect", effect.serialize())
    map
  }
}

case class CausalRelation(
  index: Int,
  negationCount: Int,
  relations: Array[Relation]
) {

  def serialize(): HashMap[String, AnyRef] = {
    val map = new HashMap[String, AnyRef]()

    map.put("index", index.asInstanceOf[JInteger])
    map.put("negationCount", index.asInstanceOf[JInteger])
    if (relations.nonEmpty)
      map.put("relations", relations.map(_.serialize()))
    map
  }
}

case class LatLon(lat: Float, lon: Float) {

  def serialize(): HashMap[String, AnyRef] = {
    val map = new HashMap[String, AnyRef]()

    map.put("lat", lat.asInstanceOf[JFloat])
    map.put("lon", lon.asInstanceOf[JFloat])
    map
  }
}

case class Location(
  name: String,
  latLonOpt: Option[LatLon]
) {

  def serialize(): HashMap[String, AnyRef] = {
    val map = new HashMap[String, AnyRef]()

    map.put("name", name)
    latLonOpt.foreach { location => map.put("location", location.serialize()) }
    map
  }
}

case class DatasetRecord(
  dataset: String,
  region: String,
  url: String,
  titleOpt: Option[String],
  terms: Array[String],
  datelineOpt: Option[String],
  dateOpt: Option[String],
  bylineOpt: Option[String],
  sentenceIndex: Int,
  sentence: String,
  causalRelations: Array[CausalRelation],
  isBelief: Boolean,
  sentimentOpt: Option[Float],
  sentenceLocations: Array[Location],
  contextBefore: String,
  contextAfter: String,
  contextLocations: Array[Location],
  prevLocations: Array[Location],
  prevDistanceOpt: Option[Int],
  nextLocations: Array[Location],
  nextDistanceOpt: Option[Int]
) {

  // See advice at https://discuss.elastic.co/t/ways-to-build-json-doc-in-es8-java-api-client/314459.
  def serialize(): HashMap[String, AnyRef] = {
    val map = new HashMap[String, AnyRef]()

    // map.put("location", LatLon(1f, 2f).serialize())
    map.put("dataset", dataset)
    map.put("region", region)
    map.put("url", url)
    titleOpt.foreach(map.put("title", _))
    if (terms.nonEmpty)
      map.put("terms", terms)
    datelineOpt.foreach(map.put("dateline", _))
    dateOpt.foreach(map.put("date", _))
    bylineOpt.foreach(map.put("byline", _))
    map.put("sentenceIndex", sentenceIndex.asInstanceOf[JInteger])
    map.put("sentence", sentence)
    if (causalRelations.nonEmpty)
      map.put("causalRelations", causalRelations.map(_.serialize()))
    map.put("isBelief", isBelief.asInstanceOf[JBoolean])
    sentimentOpt.foreach { sentiment => map.put("sentiment", sentiment.asInstanceOf[JFloat]) }
    if (sentenceLocations.nonEmpty)
      map.put("sentenceLocations", sentenceLocations.map(_.serialize()))
    if (contextBefore.nonEmpty)
      map.put("contextBefore", contextBefore)
    map.put("contextAfter", contextAfter)
    if (contextLocations.nonEmpty)
      map.put("contextLocations", contextLocations.map(_.serialize()))
    if (prevLocations.nonEmpty)
      map.put("prevLocations", prevLocations.map(_.serialize()))
    prevDistanceOpt.foreach { prevDistance => map.put("prevDistance", prevDistance.asInstanceOf[JInteger]) }
    if (nextLocations.nonEmpty)
      map.put("nextLocations", nextLocations.map(_.serialize()))
    nextDistanceOpt.foreach { nextDistance => map.put("nextDistance", nextDistance.asInstanceOf[JInteger]) }
    map
  }
}

object DatasetRecord {

  def default: DatasetRecord = {
    val datasetRecord = DatasetRecord(
      dataset = "uganda.tsv",
      region = "uganda",
      url = "http://clulab.org",
      titleOpt = Some("This article is about that"),
      terms = Array("mining", "galamsey"),
      datelineOpt = Some("September 2, 2023"),
      dateOpt = Some("2023-09-02"),
      bylineOpt = Some("Your local reporter"),
      sentenceIndex = 0,
      sentence = "The quick brown fox jumped over the lazy dog.",
      causalRelations = Array(
        CausalRelation(
          index = 1,
          negationCount = 1,
          relations = Array(
            Relation(
              cause = CauseOrEffect(
                text = "cause1-1",
                incCount = 1,
                decCount = 2,
                posCount = 3,
                negCount = 4
              ),
              effect = CauseOrEffect(
                text = "effect1-1",
                incCount = 2,
                decCount = 3,
                posCount = 4,
                negCount = 5
              )
            ),
            Relation(
              cause = CauseOrEffect(
                text = "cause1-2",
                incCount = 1,
                decCount = 2,
                posCount = 3,
                negCount = 4
              ),
              effect = CauseOrEffect(
                text = "effect1-2",
                incCount = 1,
                decCount = 2,
                posCount = 3,
                negCount = 4
              )
            )
          )
        ),
        CausalRelation(
          index = 2,
          negationCount = 1,
          relations = Array(
            Relation(
              cause = CauseOrEffect(
                text = "cause2-1",
                incCount = 1,
                decCount = 2,
                posCount = 3,
                negCount = 4
              ),
              effect = CauseOrEffect(
                text = "effect2-1",
                incCount = 2,
                decCount = 3,
                posCount = 4,
                negCount = 5
              )
            ),
            Relation(
              cause = CauseOrEffect(
                text = "cause2-2",
                incCount = 1,
                decCount = 2,
                posCount = 3,
                negCount = 4
              ),
              effect = CauseOrEffect(
                text = "effect2-2",
                incCount = 1,
                decCount = 2,
                posCount = 3,
                negCount = 4
              )
            )
          )
        )
      ),
      isBelief = true,
      sentimentOpt = Some(4.5f),
      sentenceLocations = Array(
        Location(
          name = "sentenceLocation1",
          latLonOpt = Some(LatLon(1f, 2f))
        ),
        Location(
          name = "sentenceLocation2",
          latLonOpt = Some(LatLon(3f, 4f))
        )
      ),
      contextBefore = "This is what came before",
      contextAfter = "This is what came after",
      contextLocations = Array(
        Location(
          name = "contextLocation1",
          latLonOpt = Some(LatLon(5f, 6f))
        ),
        Location(
          name = "contextLocation2",
          latLonOpt = Some(LatLon(7f, 8f))
        )
      ),
      prevLocations = Array(
        Location(
          name = "prevLocation1",
          latLonOpt = Some(LatLon(9f, 10f))
        ),
        Location(
          name = "prevLocation2",
          latLonOpt = Some(LatLon(11f, 12f))
        )
      ),
      prevDistanceOpt = Some(4),
      nextLocations = Array(
        Location(
          name = "nextLocation1",
          latLonOpt = Some(LatLon(13f, 14f))
        ),
        Location(
          name = "nextLocation2",
          latLonOpt = Some(LatLon(15f, 16f))
        )
      ),
      nextDistanceOpt = Some(5)
    )

    datasetRecord
  }
}
