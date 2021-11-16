package io.sdkman

import com.github.tomakehurst.wiremock.client.WireMock._
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpec}
import scalaj.http.Http
import support.TestNetworking

class OrphanUrlSpec extends WordSpec with Matchers with BeforeAndAfter with BeforeAndAfterAll with TestNetworking {

  val binary = "jdk-8u111-linux64.tar.gz"

  "resourceAvailable" should {

    "determine that a resource is available" in new TestValidation {
      val validUri = "/candidates/scala/2.12.4"

      stubFor(head(urlEqualTo(validUri))
        .willReturn(aResponse()
          .withHeader("content-type", "application/octet-stream")
          .withBodyFile(binary)
          .withStatus(200)))

      withClue("valid url not available") {
        resourceAvailable(httpUrlWith(validUri)) shouldBe true
      }
    }

    "determine that a resource redirecting to a valid url is available" in new TestValidation {
      val redirectUri = "/redirect/scala/2.12.5"
      val finalUri = "/finalurl/scala/2.12.5"

      stubFor(head(urlEqualTo(redirectUri))
        .willReturn(aResponse().withStatus(302).withHeader("Location", httpUrlWith(finalUri))))

      stubFor(head(urlEqualTo(finalUri))
        .willReturn(aResponse()
          .withHeader("Content-Type", "application/zip")
          .withBodyFile(binary)
          .withStatus(200)))

      withClue("redirect to a valid uri not available") {
        resourceAvailable(httpUrlWith(redirectUri)) shouldBe true
      }
    }

    "determine that a secured redirecting resource can't be reached" in new TestValidation {

      val redirectUri = "/redirect/scala/2.12.5"
      val finalUri = "/finalurl/scala/2.12.5"

      stubFor(head(urlEqualTo(redirectUri))
        .willReturn(aResponse().withStatus(302).withHeader("Location", httpUrlWith(finalUri))))

      stubFor(head(urlEqualTo(finalUri))
        .willReturn(aResponse().withStatus(403)))

      withClue("secured url should not be reachable") {
        resourceAvailable(httpUrlWith(redirectUri)) shouldBe false
      }
    }

    "determine that secured resource cannot be reached" in new TestValidation {
      val finalUri = "/finalurl/scala/2.12.5"

      stubFor(head(urlEqualTo(finalUri))
        .willReturn(aResponse().withStatus(403)))

      withClue("secured url should be reachable") {
        resourceAvailable(httpUrlWith(finalUri)) shouldBe false
      }
    }

    "determine that a resource redirects to a uri not found" in new TestValidation {
      val redirectUri = "/redirect/scala/2.12.5"
      val finalUri = "/invalid/url/scala/2.12.5"

      stubFor(head(urlEqualTo(redirectUri))
        .willReturn(aResponse().withStatus(302).withHeader("Location", httpUrlWith(finalUri))))

      stubFor(head(urlEqualTo(finalUri))
        .willReturn(aResponse().withStatus(404)))

      withClue("redirect to invalid uri available") {
        resourceAvailable(httpUrlWith(redirectUri)) shouldBe false
      }
    }

    "determine that a resource redirects to an unknown host" in new TestValidation {
      val redirectUri = "/redirect/scala/2.12.5"
      val unknownHostUrl = "http://unknown5f7c5b58a4e4e777654ad16bf641144c:9090"

      stubFor(head(urlEqualTo(redirectUri))
        .willReturn(aResponse().withStatus(302).withHeader("Location", unknownHostUrl)))

      withClue("redirect to unknown host available") {
        resourceAvailable(httpUrlWith(redirectUri)) shouldBe false
      }
    }

    "determine that a resource with invalid uri is not available" in new TestValidation {
      val invalidUri = "/candidates/scala/9.9.9"

      stubFor(head(urlEqualTo(invalidUri))
        .willReturn(aResponse()
          .withStatus(404)))

      withClue("invalid uri available") {
        resourceAvailable(httpUrlWith(invalidUri)) shouldBe false
      }
    }

    "determine that a resource with html content type is not available" in new TestValidation {
      val validUri = "/candidate/java/10.0.1"

      stubFor(head(urlEqualTo(validUri))
        .willReturn(aResponse()
          .withBody("<html>bogus</html>")
          .withHeader("Content-Type", "text/html")
          .withStatus(200)))

      withClue("text/html available") {
        resourceAvailable(httpUrlWith(validUri)) shouldBe false
      }
    }

    "determine that a resource with unknown host is not available" in new TestValidation {
      val unknownHostUrl = "http://unknown5f7c5b58a4e4e777654ad16bf641144c:9090"

      withClue("unknown host available") {
        resourceAvailable(unknownHostUrl) shouldBe false
      }
    }

    "determine that a resource timed out beyond an acceptable timeframe" in new TestValidation {

      override val connTimeout = 500

      override val readTimeout = 500

      val validUri = "/candidate/java/10.0.1"

      stubFor(head(urlEqualTo(validUri))
        .willReturn(aResponse()
          .withFixedDelay(1000)
          .withHeader("content-type", "application/octet-stream")
          .withBodyFile(binary)
          .withStatus(200)))

      withClue("resource did not timeout") {
        resourceAvailable(httpUrlWith(validUri)) shouldBe false
      }
    }
  }

  private class TestValidation extends UrlValidation with LazyLogging
}
