package earthquakes

import connector.EarthquakeConnector
import domain.Config
import domain.Models.GeoJsonResponse

import cats.effect._
import com.comcast.ip4s.IpLiteralSyntax
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.jsonOf
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server._
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    Server.serverStream.useForever.as(ExitCode.Success)
}

object Server {
  def serverStream: Resource[IO, Server] = {
    implicit val geoJsonResponseJsonDecoder: EntityDecoder[IO, GeoJsonResponse] = jsonOf[IO, GeoJsonResponse]

    val config = ConfigSource.default.loadOrThrow[Config]
    val client = EmberClientBuilder.default[IO].build
    val connector = new EarthquakeConnector(config.usgs, client)
    val service = new EarthquakeService(connector)
    val routes = new EarthquakeRoutes(service).routes

    EmberServerBuilder
      .default[IO]
      .withPort(port"8080")
      .withHost(host"0.0.0.0")
      .withHttpApp(routes.orNotFound)
      .build
  }
}
