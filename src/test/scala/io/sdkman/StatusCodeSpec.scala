package io.sdkman

import org.scalatest.{BeforeAndAfter, Matchers, TryValues, WordSpec}

class StatusCodeSpec extends WordSpec with Matchers with BeforeAndAfter with TryValues {
  "statusCode" should {
    "report a 200 status code" in new UrlValidation {
      resolvedStatusCode("https://cdn.azul.com/zulu/bin/zulu10.2+3-jdk10.0.1-linux_x64.tar.gz").success.value shouldBe 200
      resolvedStatusCode("https://github.com/sbt/sbt/releases/download/v1.1.4/sbt-1.1.4.zip").success.value shouldBe 200
    }
  }
}
