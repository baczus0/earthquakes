package earthquakes

import domain.Models.{Coordinates, Earthquake}

import cats.data.Kleisli
import cats.effect._
import cats.effect.unsafe.implicits.global
import io.circe.generic.auto._
import io.circe.parser._
import org.http4s._
import org.http4s.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

import java.time.LocalDate

class EarthquakeRoutesTest extends AnyFunSuite with MockFactory {

  val mockService: EarthquakeService = stub[EarthquakeService]
  val routes: Kleisli[IO, Request[IO], Response[IO]] = new EarthquakeRoutes(mockService).routes.orNotFound

  test("GET /earthquake/closest returns 200 with earthquake") {

    val earthquake = Earthquake(5.0, LocalDate.now(), Coordinates(50.5, 30.5))
    (mockService.findClosestEarthquake _).when(*, *, *, *).returns(IO.pure(Some(earthquake)))

    val request =
      Request[IO](Method.GET, uri"/earthquake/closest".withQueryParam("latitude", "50.5").withQueryParam("longitude", "30.5"))
    val response = routes.run(request).unsafeRunSync()

    assert(response.status === Status.Ok)

    val body = response.body.compile.toVector.unsafeRunSync()
    parse(new String(body.toArray)) match {
      case Left(failure) => fail(s"Failed to parse JSON: $failure")
      case Right(json) =>
        assert(json.as[Earthquake].toOption.get === earthquake)
    }
  }

  test("GET /earthquake/closest returns 404 when no earthquake") {

    (mockService.findClosestEarthquake _).when(*, *, *, *).returns(IO.pure(None))

    val request =
      Request[IO](Method.GET, uri"/earthquake/closest".withQueryParam("latitude", "50.5").withQueryParam("longitude", "30.5"))
    val response = routes.run(request).unsafeRunSync()

    assert(response.status === Status.NotFound)
  }

  test("GET /earthquake/strongest returns 200 with list of earthquakes") {

    val earthquakes = List(
      Earthquake(5.0, LocalDate.now(), Coordinates(50.5, 30.5)),
      Earthquake(6.0, LocalDate.now(), Coordinates(51.5, 31.5))
    )
    (mockService.findStrongestEarthquakes _).when(*, *, *).returns(IO.pure(earthquakes))

    val request = Request[IO](
      Method.GET,
      uri"/earthquake/strongest"
        .withQueryParam("page", "0")
        .withQueryParam("pageSize", "10")
    )
    val response = routes.run(request).unsafeRunSync()

    assert(response.status === Status.Ok)

    val body = response.body.compile.toVector.unsafeRunSync()
    parse(new String(body.toArray)) match {
      case Left(failure) => fail(s"Failed to parse JSON: $failure")
      case Right(json) =>
        assert(json.as[List[Earthquake]].toOption.get === earthquakes)
    }
  }

  test("GET /earthquake/strongest/date returns 200 with list of earthquakes") {

    val earthquakes = List(
      Earthquake(5.0, LocalDate.now(), Coordinates(50.5, 30.5)),
      Earthquake(6.0, LocalDate.now(), Coordinates(51.5, 31.5))
    )
    (mockService.findStrongestEarthquakesAtDate _).when(*, *, *, *).returns(IO.pure(earthquakes))

    val request = Request[IO](
      Method.GET,
      uri"/earthquake/strongest/date"
        .withQueryParam("date", "2023-01-01")
        .withQueryParam("page", "0")
        .withQueryParam("pageSize", "10")
    )
    val response = routes.run(request).unsafeRunSync()

    assert(response.status === Status.Ok)

    val body = response.body.compile.toVector.unsafeRunSync()
    parse(new String(body.toArray)) match {
      case Left(failure) => fail(s"Failed to parse JSON: $failure")
      case Right(json) =>
        assert(json.as[List[Earthquake]].toOption.get === earthquakes)
    }
  }

  test("GET /earthquake/strongest/date returns 400 when service return IllegalArgumentException") {

    val request = Request[IO](
      Method.GET,
      uri"/earthquake/strongest/date"
        .withQueryParam("date", "2023-01-01")
        .withQueryParam("count", "101")
        .withQueryParam("page", "0")
        .withQueryParam("pageSize", "10")
    )
    (mockService.findStrongestEarthquakesAtDate _).when(*, *, *, *).returns(IO.raiseError(new IllegalArgumentException("error")))

    val response = routes.run(request).unsafeRunSync()

    assert(response.status === Status.BadRequest)
  }
}
