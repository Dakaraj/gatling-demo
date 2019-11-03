package baseConfig

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

class BaseSimulation extends Simulation {
//  val host = "https://challengers.flood.io"
  val host = "https://training.flooded.io"

  val httpConf: HttpProtocolBuilder = http
    .baseUrl(host)
    .userAgentHeader("I AM ROBOT")
    .acceptEncodingHeader("gzip, deflate, br")
    .proxy(Proxy("localhost", 8888).httpsPort(8888))
}
