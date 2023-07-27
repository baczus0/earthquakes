package earthquakes
package domain


case class Config(usgs: USGSConfig, server: ServerConfig)

case class USGSConfig(apiUrl: String)

case class ServerConfig(host: String,
                         port: String
                       )
