package support

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}

trait TestNetworking {

  self: BeforeAndAfter with BeforeAndAfterAll =>

  lazy val wireMockServer = new WireMockServer(wireMockConfig().port(8080))


  override def beforeAll(): Unit = wireMockServer.start()

  override def afterAll(): Unit = wireMockServer.stop()

  before {
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
