# Earthquake Data Service

This service provides earthquake data in JSON format based on different parameters. It's implemented using Scala with
functional programming principles.

## Running

Run `sbt run` to start the service.

## Endpoints

1. **GET /earthquake/closest**
    - Find the closest and strongest earthquake based on your location and other optional parameters.
    - Query Parameters:
        - longitude: required
        - latitude: required
        - minMag: optional, minimal power of the closest earthquake.
        - days: optional, during the last N days. If not provided, defaults to last 30 days.

2. **GET /earthquake/strongest**
    - Find the strongest earthquakes during the last X days.
    - Query Parameters:
        - days: optional, during the last X days. If not provided, defaults to the last 30 days.
        - page: required, page number for pagination.
        - pageSize: required, number of results per page.

3. **GET /earthquake/strongest/date**
    - Find the top N strongest earthquakes for a given day.
    - Query Parameters:
        - date: required, day in format `2013-12-30`.
        - count: optional, number of strongest earthquakes to return. Defaults to 100.
        - page: required, page number for pagination.
        - pageSize: required, number of results per page.

Note: Returns HTTP error if the date is not within the last 30 days or count parameter is more than 100.

## Tests

You can run tests by using the command `sbt test`.

## Data Source

Earthquake data is fetched from the USGS Earthquake Catalog API.
For more information about the data, please
visit [USGS Earthquake Catalog API](https://earthquake.usgs.gov/fdsnws/event/1/)
and [USGS GeoJson Earthquake Feed](https://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php).
