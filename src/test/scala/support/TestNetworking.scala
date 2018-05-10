package support

import com.github.tomakehurst.wiremock.client.WireMock
import org.scalatest.BeforeAndAfter

trait TestNetworking {

  self: BeforeAndAfter =>

  before {
    println("resetting...")
    WireMock.reset()
  }

  val WiremockHost = "localhost"
  val WiremockPort = 8080

  val MongoHost = "localhost"
  val MongoPort = 27017

  val GreenmailHost = "localhost"
  val GreenmailPort = 3143

  WireMock.configureFor(WiremockHost, WiremockPort)

  def urlWith(uri: String) = s"http://$WiremockHost:$WiremockPort$uri"

}
