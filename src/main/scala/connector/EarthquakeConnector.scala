package earthquakes
package connector

import domain.Models.GeoJsonResponse
import domain.USGSConfig

import cats.effect._
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.{Uri, _}

import java.time.LocalDate

class EarthquakeConnector(usgsConfig: USGSConfig, client: Resource[IO, Client[IO]])(
  implicit val jsonDecoder: EntityDecoder[IO, GeoJsonResponse]
) {

  def getEarthquakes(
                      startTime: Option[LocalDate],
                      endTime: Option[LocalDate],
                      latitude: Option[Double],
                      longitude: Option[Double],
                      maxRadiusKm: Option[Double],
                      minMagnitude: Option[Double],
                      limit: Option[Int],
                      offset: Option[Int]
                    ): IO[GeoJsonResponse] = {
    val uri = Uri
      .unsafeFromString(usgsConfig.apiUrl)
      .withQueryParam("format", "geojson")
      .withQueryParam("starttime", startTime.getOrElse(LocalDate.now().minusDays(30)).toString)
      .withQueryParam("endtime", endTime.getOrElse(LocalDate.now().plusDays(1)).toString)
      .withQueryParam("latitude", latitude.getOrElse(0.0))
      .withQueryParam("longitude", longitude.getOrElse(0.0))
      .withQueryParam("maxradiuskm", maxRadiusKm.getOrElse(20000.0))
      .withQueryParam("minmagnitude", minMagnitude.getOrElse(0.0))
      .withQueryParam("limit", limit.getOrElse(20000))
      .withQueryParam("offset", offset.getOrElse(1))
      .withQueryParam("orderby", "magnitude")
    client.use(_.expect[GeoJsonResponse](GET(uri)))
  }
}
