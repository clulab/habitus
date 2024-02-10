package org.clulab.habitus.elasticsearch.data

import java.lang.{Float => JFloat}
import java.util.HashMap

case class LatLon(lat: Float, lon: Float) {

  def serialize(): HashMap[String, AnyRef] = {
    val map = new HashMap[String, AnyRef]()

    map.put("lat", lat.asInstanceOf[JFloat])
    map.put("lon", lon.asInstanceOf[JFloat])
    map
  }
}
