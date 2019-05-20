package io.sdkman

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{BeforeAndAfter, Matchers, TryValues, WordSpec}

class StatusCodeSpec extends WordSpec with Matchers with BeforeAndAfter with TryValues {
  "statusCode" should {
    "report a 200 status code for a normal url" in new TestValidation {
      resolvedStatusCode("https://cdn.azul.com/zulu/bin/zulu10.2+3-jdk10.0.1-linux_x64.tar.gz").success.value shouldBe 200
    }

    "report a 200 status code for a github url" in new TestValidation {
      resolvedStatusCode("https://github.com/sbt/sbt/releases/download/v1.1.4/sbt-1.1.4.zip").success.value shouldBe 200
    }

    "report a 200 status code for a valid oracle url" ignore new TestValidation {
      resolvedStatusCode(
        "http://download.oracle.com/otn-pub/java/jdk/10.0.1+10/fb4372174a714e6b8c52526dc134031e/jdk-10.0.1_linux-x64_bin.tar.gz",
        Some(Cookie("oraclelicense", "accept-securebackup-cookie"))).success.value shouldBe 200
    }

    "report a failure for an invalid oracle url" ignore new TestValidation {
      resolvedStatusCode(
        "http://download.oracle.com/otn-pub/java/jdk/10.0.0+1/fb4372174a714e6b8c52526dc134031e/jdk-10.0.0_linux-x64_bin.tar.gz",
        Some(Cookie("oraclelicense", "accept-securebackup-cookie"))).isFailure shouldBe true
    }
  }

  private class TestValidation extends UrlValidation with LazyLogging

}
