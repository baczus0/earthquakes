package earthquakes
package domain

case class Config(usgs: USGSConfig)

case class USGSConfig(apiUrl: String)
