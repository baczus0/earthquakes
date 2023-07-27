package earthquakes

import domain.Models.GeoJsonResponse
import domain.USGSConfig

import cats.effect._
import org.http4s.Method.GET
import org.http4s.{Uri, _}
import org.http4s.client.Client
import org.http4s.client.dsl.io._
import org.http4s.client.middleware.RequestLogger

import java.time.LocalDate

class EarthquakeConnector(usgsConfig: USGSConfig, client: Resource[IO, Client[IO]]) (
  implicit val jsonDecoder: EntityDecoder[IO, GeoJsonResponse]
) {


  def getEarthquakes(startTime: LocalDate, latitude: Double, longitude: Double, maxRadiusKm: Int, minMagnitude: Double) : IO[GeoJsonResponse] = {
    val url = usgsConfig.apiUrl
    val uri = Uri.unsafeFromString(url)
      .withQueryParam("format", "geojson")
      .withQueryParam("starttime", startTime.toString)
      .withQueryParam("latitude", latitude)
      .withQueryParam("longitude", longitude)
      .withQueryParam("maxradiuskm", maxRadiusKm)
      .withQueryParam("minmagnitude", minMagnitude)
//      .withQueryParam("orderby", "time")
    val value = client.use(client => {
      val dd = RequestLogger(logHeaders = true, logBody = true)(client)
      val res = dd.expect[GeoJsonResponse](GET(uri))
      res
    })
    value

  }

}
