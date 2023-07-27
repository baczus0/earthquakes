package earthquakes

import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import io.circe.generic.auto._
import cats.effect._
import cats.implicits.toFlatMapOps
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io._


class EarthquakeRoutes(service: EarthquakeService) extends Http4sDsl[IO] {

  val routes: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "ping" => Ok("ping")
      case GET -> Root / "earthquake" :? LatQueryParamMatcher(latitude) +& LonQueryParamMatcher(longitude)
        +& MinMagQueryParamMatcher(minMag) +& DaysQueryParamMatcher(days) =>
        service.getEarthquakes(longitude, latitude, minMag, days)
                .flatMap(d => Ok(d.toString()))
//          .flatMap {
//            case Some(earthquake) => Ok("ping")
//            case None => NotFound()
//          }
    }
}


object LatQueryParamMatcher extends QueryParamDecoderMatcher[Double]("latitude")

object LonQueryParamMatcher extends QueryParamDecoderMatcher[Double]("longitude")

object MinMagQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Double]("minMag")

object DaysQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("days")


