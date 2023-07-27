package earthquakes
package domain

import java.time.Instant

object Models {
  case class Earthquake(mag: Double, time: Long, latitude: Double, longitude: Double)

  case class GeoJsonResponse(features: List[Feature])

  case class Feature(properties: Property, geometry: Geometry)

  case class Property(mag: Double, time: Long)

  case class Geometry(coordinates: List[Double])
}