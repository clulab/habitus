package org.clulab.habitus.elasticsearch.data

import java.util.HashMap

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
