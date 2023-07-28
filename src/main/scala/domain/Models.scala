package earthquakes
package domain

import java.time.LocalDate

object Models {
  case class Earthquake(magnitude: Double, date: LocalDate, coordinates: Coordinates)

  case class Coordinates(latitude: Double, longitude: Double)

  case class GeoJsonResponse(features: List[Feature])

  case class Feature(properties: Property, geometry: Geometry)

  case class Property(mag: Double, time: Long)

  case class Geometry(coordinates: List[Double])
}
