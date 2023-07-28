package earthquakes

import connector.EarthquakeConnector
import domain.Models.{Coordinates, Earthquake, Feature, GeoJsonResponse}

import cats.effect._
import cats.implicits._

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, ZoneId}

class EarthquakeService(connector: EarthquakeConnector) {

  def findClosestEarthquake(longitude: Double, latitude: Double, minMag: Option[Double], days: Option[Int]): IO[Option[Earthquake]] = {
    val daysAgo = days.getOrElse(30)
    val startTime = LocalDate.now().minusDays(daysAgo)
    val minMagnitude = minMag.getOrElse(0.0)
    val userCoordinates = Coordinates(latitude, longitude)
    for {
      response <- connector.getEarthquakes(
        startTime = Option(startTime),
        endTime = None,
        latitude = Option(latitude),
        longitude = Option(longitude),
        maxRadiusKm = None,
        minMagnitude = Option(minMagnitude),
        limit = None,
        offset = None
      )
      earthquakes = GeoJsonResponseMapper.map(response)
      closestEarthquake = earthquakes
        .map(eq => (eq, calculateDistance(userCoordinates, eq.coordinates)))
        .minByOption { case (_, distance) => distance }
        .map(_._1)
    } yield closestEarthquake
  }

  def findStrongestEarthquakes(days: Option[Int], page: Int, pageSize: Int): IO[List[Earthquake]] = {
    val daysAgo = days.getOrElse(30)
    val startTime = LocalDate.now().minusDays(daysAgo)
    for {
      response <- connector.getEarthquakes(
        startTime = Option(startTime),
        endTime = None,
        latitude = None,
        longitude = None,
        maxRadiusKm = None,
        minMagnitude = None,
        limit = Option(pageSize),
        offset = Option(page * pageSize + 1)
      )
    } yield GeoJsonResponseMapper.map(response)
  }

  def findStrongestEarthquakesAtDate(date: LocalDate, count: Option[Int], page: Int, pageSize: Int): IO[List[Earthquake]] =
    (ChronoUnit.DAYS.between(date, LocalDate.now()) < 30, count.getOrElse(100) <= 100) match {
      case (true, true) =>
        val elemCount = count.getOrElse(100)
        for {
          response <- connector.getEarthquakes(
            startTime = Option(date),
            endTime = Option(date.plusDays(1)),
            latitude = None,
            longitude = None,
            maxRadiusKm = None,
            minMagnitude = None,
            limit = Option(Math.min(pageSize, elemCount)),
            offset = Option(page * pageSize + 1)
          )
        } yield GeoJsonResponseMapper.map(response)

      case (false, _) =>
        IO.raiseError(new IllegalArgumentException("The date must be within the last 30 days"))

      case (_, false) =>
        IO.raiseError(new IllegalArgumentException("Count parameter cannot be more than 100"))
    }

  private def calculateDistance(c1: Coordinates, c2: Coordinates): Double = {
    val earthRadiusKm = 6371
    val dLat = degreesToRadians(c2.latitude - c1.latitude)
    val dLon = degreesToRadians(c2.longitude - c1.longitude)

    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(degreesToRadians(c1.latitude)) * Math.cos(degreesToRadians(c2.latitude))
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    earthRadiusKm * c
  }

  private def degreesToRadians(degrees: Double): Double =
    degrees * (Math.PI / 180)
}

object GeoJsonResponseMapper {
  def map(response: GeoJsonResponse): List[Earthquake] =
    response.features.map(map)

  def map(feature: Feature): Earthquake =
    Earthquake(
      magnitude = feature.properties.mag,
      date = Instant.ofEpochMilli(feature.properties.time).atZone(ZoneId.of("UTC")).toLocalDate,
      coordinates = Coordinates(latitude = feature.geometry.coordinates(1), longitude = feature.geometry.coordinates(0))
    )
}
