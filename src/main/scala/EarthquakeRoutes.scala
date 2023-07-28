package earthquakes

import cats.effect._
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl

import java.time.LocalDate

class EarthquakeRoutes(service: EarthquakeService) extends Http4sDsl[IO] {

  implicit val localDateQueryParamDecoder: QueryParamDecoder[LocalDate] =
    QueryParamDecoder[String].map(LocalDate.parse)

  private def handleErrorWithResponse: PartialFunction[Throwable, IO[Response[IO]]] = {
    case _: IllegalArgumentException => BadRequest()
    case e => InternalServerError(e.getMessage)
  }

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "earthquake" / "closest"
      :? LatQueryParamMatcher(latitude)
      +& LonQueryParamMatcher(longitude)
      +& MinMagQueryParamMatcher(minMag)
      +& DaysQueryParamMatcher(days) =>
      (for {
        earthquakeOpt <- service.findClosestEarthquake(longitude, latitude, minMag, days)
        response <- earthquakeOpt match {
          case Some(earthquake) => Ok(earthquake.asJson)
          case None => NotFound()
        }
      } yield response).handleErrorWith(handleErrorWithResponse)

    case GET -> Root / "earthquake" / "strongest"
      :? DaysQueryParamMatcher(days)
      +& PageQueryParamMatcher(page)
      +& PageSizeQueryParamMatcher(pageSize) =>
      (for {
        earthquakes <- service.findStrongestEarthquakes(days, page, pageSize)
        response <- Ok(earthquakes.asJson)
      } yield response).handleErrorWith(handleErrorWithResponse)

    case GET -> Root / "earthquake" / "strongest" / "top"
      :? DateQueryParamMatcher(date)
      +& CountQueryParamMatcher(count)
      +& PageQueryParamMatcher(page)
      +& PageSizeQueryParamMatcher(pageSize) =>
      (for {
        earthquakes <- service.findStrongestEarthquakesAtDate(date, count, page, pageSize)
        response <- Ok(earthquakes.asJson)
      } yield response).handleErrorWith(handleErrorWithResponse)
  }

  object LatQueryParamMatcher extends QueryParamDecoderMatcher[Double]("latitude")

  object LonQueryParamMatcher extends QueryParamDecoderMatcher[Double]("longitude")

  object MinMagQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Double]("minMagnitude")

  object DaysQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("days")

  object DateQueryParamMatcher extends QueryParamDecoderMatcher[LocalDate](name = "date")

  object CountQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("count")

  object PageQueryParamMatcher extends QueryParamDecoderMatcher[Int]("page")

  object PageSizeQueryParamMatcher extends QueryParamDecoderMatcher[Int]("pageSize")
}
