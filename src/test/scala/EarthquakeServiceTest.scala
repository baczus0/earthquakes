package earthquakes

import connector.EarthquakeConnector
import domain.Models._

import cats.effect._
import cats.effect.unsafe.implicits.global
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class EarthquakeServiceTest extends AnyFunSuite with MockFactory {

  val mockConnector: EarthquakeConnector = stub[EarthquakeConnector]
  val service = new EarthquakeService(mockConnector)

  test("findClosestEarthquake should return closest earthquake") {

    val list = List(
      Feature(Property(3.5, 1630685042000L), Geometry(List(30.5, 50.4))),
      Feature(Property(3.7, 1630685042001L), Geometry(List(38.5, 52.4)))
    )
    val expectedResponse = GeoJsonResponse(list)
    (mockConnector.getEarthquakes _).when(*, *, *, *, *, *, *, *).returns(IO.pure(expectedResponse))

    val actual = service.findClosestEarthquake(30.6, 50.5, Some(3.0), Some(30)).unsafeRunSync()

    assert(actual.isDefined)
    assert(actual.get.coordinates.latitude === 50.4)
    assert(actual.get.coordinates.longitude === 30.5)
  }

  test("findClosestEarthquake should return empty when no proper earthquake") {

    val expectedResponse = GeoJsonResponse(List.empty)
    (mockConnector.getEarthquakes _).when(*, *, *, *, *, *, *, *).returns(IO.pure(expectedResponse))

    val actual = service.findClosestEarthquake(50.5, 30.6, Some(4.0), Some(30)).unsafeRunSync()

    assert(actual.isEmpty)
  }

  test("findClosestEarthquake should return strongest, closest earthquake") {

    val list = List(
      Feature(Property(3.7, 1630685042001L), Geometry(List(2.0, 1.0))),
      Feature(Property(3.6, 1630685042000L), Geometry(List(1.0, 1.0))),
      Feature(Property(3.5, 1630685042000L), Geometry(List(1.0, 1.0)))
    )
    val expectedResponse = GeoJsonResponse(list)
    (mockConnector.getEarthquakes _).when(*, *, *, *, *, *, *, *).returns(IO.pure(expectedResponse))

    val actual = service.findClosestEarthquake(0.0, 0.0, Some(3.0), Some(30)).unsafeRunSync()

    assert(actual.isDefined)
    assert(actual.get.coordinates.latitude === 1.0)
    assert(actual.get.coordinates.longitude === 1.0)
    assert(actual.get.magnitude === 3.6)
  }

  test("findStrongestEarthquakes should return strongest earthquakes") {

    val list = List(
      Feature(Property(3.7, 1630685042001L), Geometry(List(38.5, 52.4))),
      Feature(Property(3.5, 1630685042000L), Geometry(List(30.5, 50.4)))
    )
    val expectedResponse = GeoJsonResponse(list)
    (mockConnector.getEarthquakes _).when(*, *, *, *, *, *, *, *).returns(IO.pure(expectedResponse))

    val actual = service.findStrongestEarthquakes(Some(30), 0, 10).unsafeRunSync()

    assert(actual.nonEmpty)
    assert(actual(0).magnitude === 3.7)
    assert(actual(1).magnitude === 3.5)
  }

  test("findStrongestEarthquakes should return empty when no earthquakes") {

    val expectedResponse = GeoJsonResponse(List.empty)
    (mockConnector.getEarthquakes _).when(*, *, *, *, *, *, *, *).returns(IO.pure(expectedResponse))

    val actual = service.findStrongestEarthquakes(Some(30), 0, 10).unsafeRunSync()

    assert(actual.isEmpty)
  }

  test("findStrongestEarthquakes should return specified number of strongest earthquakes") {

    val list = List(
      Feature(Property(3.7, 1630685042001L), Geometry(List(2.0, 1.0))),
      Feature(Property(3.6, 1630685042000L), Geometry(List(1.0, 1.0)))
    )
    val expectedResponse = GeoJsonResponse(list)
    (mockConnector.getEarthquakes _).when(*, *, *, *, *, *, *, *).returns(IO.pure(expectedResponse))

    val actual = service.findStrongestEarthquakes(Some(30), 0, 2).unsafeRunSync()

    assert(actual.size == 2)
    assert(actual(0).magnitude === 3.7)
    assert(actual(1).magnitude === 3.6)
  }

  import java.time.LocalDate

  test("findStrongestEarthquakesAtDate should return strongest earthquakes for given date") {

    val list = List(
      Feature(Property(5.0, 1630685042002L), Geometry(List(40.5, 55.4))),
      Feature(Property(4.7, 1630685042001L), Geometry(List(38.5, 52.4))),
      Feature(Property(4.5, 1630685042000L), Geometry(List(30.5, 50.4)))
    )
    val expectedResponse = GeoJsonResponse(list)
    (mockConnector.getEarthquakes _).when(*, *, *, *, *, *, *, *).returns(IO.pure(expectedResponse))

    val actual = service.findStrongestEarthquakesAtDate(LocalDate.now(), Some(2), 0, 10).unsafeRunSync()

    assert(actual.nonEmpty)
    assert(actual.head.magnitude === 5.0)
  }

  test("findStrongestEarthquakesAtDate should return empty when no earthquakes for given date") {

    val expectedResponse = GeoJsonResponse(List.empty)
    (mockConnector.getEarthquakes _).when(*, *, *, *, *, *, *, *).returns(IO.pure(expectedResponse))

    val actual = service.findStrongestEarthquakesAtDate(LocalDate.now(), Some(2), 0, 10).unsafeRunSync()

    assert(actual.isEmpty)
  }

  test("findStrongestEarthquakesAtDate should return specified number of strongest earthquakes for given date") {

    val list = List(
      Feature(Property(5.0, 1630685042002L), Geometry(List(40.5, 55.4))),
      Feature(Property(4.7, 1630685042001L), Geometry(List(38.5, 52.4)))
    )
    val expectedResponse = GeoJsonResponse(list)
    (mockConnector.getEarthquakes _).when(*, *, *, *, *, *, *, *).returns(IO.pure(expectedResponse))

    val actual = service.findStrongestEarthquakesAtDate(LocalDate.now(), Some(2), 1, 2).unsafeRunSync()

    assert(actual.size == 2)
    assert(actual(0).magnitude === 5.0)
    assert(actual(1).magnitude === 4.7)
  }

  test("findStrongestEarthquakesAtDate should raise error when date is more than 30 days ago") {
    val thirtyOneDaysAgo = LocalDate.now().minusDays(31)
    val exception = intercept[IllegalArgumentException] {
      service.findStrongestEarthquakesAtDate(thirtyOneDaysAgo, Some(2), 0, 10).unsafeRunSync()
    }
    assert(exception.getMessage == "The date must be within the last 30 days")
  }

  test("findStrongestEarthquakesAtDate should raise error when count is more than 100") {
    val exception = intercept[IllegalArgumentException] {
      service.findStrongestEarthquakesAtDate(LocalDate.now(), Some(101), 0, 10).unsafeRunSync()
    }
    assert(exception.getMessage == "Count parameter cannot be more than 100")
  }

}
