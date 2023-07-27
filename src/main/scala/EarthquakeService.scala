package earthquakes

import cats.effect._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.client._
import org.http4s.client.dsl.io._
import domain.Models.{Earthquake, GeoJsonResponse}
import org.http4s.Method.GET

import java.time.{Instant, LocalDate}
import java.time.temporal.{ChronoUnit, TemporalUnit}

class EarthquakeService(connector: EarthquakeConnector) {

  def getEarthquakes(longitude: Double, latitude: Double, minMag: Option[Double], days: Option[Int]): IO[List[Earthquake]] = {
    val daysAgo = days.getOrElse(30)
    val minMagnitude = minMag.getOrElse(0.0)
    val startTime = LocalDate.now().minus(daysAgo, ChronoUnit.DAYS)
    val maxRadiusKm = 1000
    val response = connector.getEarthquakes(startTime, latitude, longitude, maxRadiusKm, minMagnitude)
    response.map { response => GeoJsonResponseMapper.map(response) }
  }

  //      response.flatMap {r => r.features
  //        .map(feature => (calculateDistance(longitude, latitude, feature.geometry.coordinates), feature.properties))
  //        .sortBy { case (distance, earthquake) => (distance, -earthquake.mag) }
  //        .headOption
  //        .map(_._2)

  def calculateDistance(longitude1: Double, latitude1: Double, coordinates: List[Double]): Double = {
    val longitude2 = coordinates(0)
    val latitude2 = coordinates(1)
    val earthRadiusKm = 6371
    val dLat = degreesToRadians(latitude2 - latitude1)
    val dLon = degreesToRadians(longitude2 - longitude1)

    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(degreesToRadians(latitude1)) * Math.cos(degreesToRadians(latitude2))
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    earthRadiusKm * c
  }

  def degreesToRadians(degrees: Double): Double = {
    degrees * (Math.PI / 180)
  }

  object GeoJsonResponseMapper {
    def map(response: GeoJsonResponse): List[Earthquake] = {
      response.features.map {
        feature => Earthquake(feature.properties.mag, feature.properties.time, feature.geometry.coordinates(1), feature.geometry.coordinates(0))
      }
    }
  }


}

