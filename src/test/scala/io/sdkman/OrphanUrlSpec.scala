package io.sdkman

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import support.TestNetworking

class OrphanUrlSpec extends WordSpec with Matchers with BeforeAndAfter with TestNetworking {

  val binary = "/jdk-8u111-linux64.tar.gz"

  "hasOrphanUrl" should {

    "determine that a resource is not orphaned" in new UrlValidation {
      val validUri = "/candidates/scala/2.12.4"

      stubFor(get(urlEqualTo(validUri))
        .willReturn(aResponse()
          .withHeader("Content-Type", "application/zip")
          .withBodyFile(binary)
          .withStatus(200)))

      withClue("valid url orphaned") {
        hasOrphanedUrl(urlWith(validUri)) shouldBe false
      }
    }

    "determine that a resource redirecting to a valid uri is not orphaned" in new UrlValidation {
      val redirectUri = "/redirect/scala/2.12.5"
      val finalUri = "/finalurl/scala/2.12.5"

      stubFor(get(urlEqualTo(redirectUri))
        .willReturn(aResponse().withStatus(302).withHeader("Location", urlWith(finalUri))))

      stubFor(get(urlEqualTo(finalUri))
        .willReturn(aResponse()
          .withHeader("Content-Type", "application/zip")
          .withBodyFile(binary)
          .withStatus(200)))

      withClue("redirect to a valid uri orphaned") {
        hasOrphanedUrl(urlWith(redirectUri)) shouldBe false
      }
    }

    "determine that a secured redirecting resource can be reached" in new UrlValidation {

      val redirectUri = "/redirect/scala/2.12.5"
      val finalUri = "/finalurl/scala/2.12.5"

      val secureCookineName = "some_key"
      val secureCookieValue = "some_value"

      stubFor(get(urlEqualTo(redirectUri))
        .willReturn(aResponse().withStatus(302).withHeader("Location", urlWith(finalUri))))

      stubFor(get(urlEqualTo(finalUri))
        .willReturn(aResponse()
          .withHeader("Content-Type", "application/zip")
          .withBodyFile(binary)
          .withStatus(200)))

      withClue("secured url should be reachable") {
        hasOrphanedUrl(urlWith(redirectUri), Some(Cookie(secureCookineName, secureCookieValue))) shouldBe false
      }
    }

    "determine that a secured redirecting resource can't be reached" in new UrlValidation {

      val redirectUri = "/redirect/scala/2.12.5"
      val finalUri = "/finalurl/scala/2.12.5"

      stubFor(get(urlEqualTo(redirectUri))
        .willReturn(aResponse().withStatus(302).withHeader("Location", urlWith(finalUri))))

      stubFor(get(urlEqualTo(finalUri))
        .willReturn(aResponse().withStatus(403)))

      withClue("secured url should not be reachable") {
        hasOrphanedUrl(urlWith(redirectUri)) shouldBe true
      }
    }

    "determine that a resource redirects to a uri not found" in new UrlValidation {
      val redirectUri = "/redirect/scala/2.12.5"
      val finalUri = "/invalid/url/scala/2.12.5"

      stubFor(get(urlEqualTo(redirectUri))
        .willReturn(aResponse().withStatus(302).withHeader("Location", urlWith(finalUri))))

      stubFor(get(urlEqualTo(finalUri))
        .willReturn(aResponse().withStatus(404)))

      withClue("redirect to invalid uri not orphaned") {
        hasOrphanedUrl(urlWith(redirectUri)) shouldBe true
      }
    }

    "determine that a resource redirects to an unknown host" in new UrlValidation {
      val redirectUri = "/redirect/scala/2.12.5"
      val unknownHostUrl = "http://unknown5f7c5b58a4e4e777654ad16bf641144c:9090"

      stubFor(get(urlEqualTo(redirectUri))
        .willReturn(aResponse().withStatus(302).withHeader("Location", unknownHostUrl)))

      withClue("redirect to unknown host not orphaned") {
        hasOrphanedUrl(urlWith(redirectUri)) shouldBe true
      }
    }

    "determine that a resource with invalid uri is orphaned" in new UrlValidation {
      val invalidUri = "/candidates/scala/9.9.9"

      stubFor(get(urlEqualTo(invalidUri))
        .willReturn(aResponse()
          .withStatus(404)))

      withClue("invalid uri not orphaned") {
        hasOrphanedUrl(urlWith(invalidUri)) shouldBe true
      }
    }

    "determine that a resource with no content is orphaned" in new UrlValidation {
      val validUri = "/candidate/scala/1.2.3"

      stubFor(get(urlEqualTo(validUri))
        .willReturn(aResponse()
          .withBody(new Array[Byte](0))
          .withStatus(200)))

      withClue("empty stream not orphaned") {
        hasOrphanedUrl(urlWith(validUri)) shouldBe true
      }
    }

    "deterimine that a resource with unknown host is orphaned" in new UrlValidation {
      val unknownHostUrl = "http://unknown5f7c5b58a4e4e777654ad16bf641144c:9090"

      withClue("unknown host not orphaned") {
        hasOrphanedUrl(unknownHostUrl) shouldBe true
      }
    }
  }
}