package io.sdkman

import java.io.{InputStream, PushbackInputStream}

import scalaj.http.Http
import scalaj.http.HttpOptions.followRedirects

import scala.util.Try

case class Cookie(name: String, value: String)

trait UrlValidation {
  def resourceAvailable(url: String, cookie: Option[Cookie] = None): Boolean =
    resolvedStatusCode(url, cookie)
      .fold(e => false, code => !Seq(404, 403, 401).contains(code))

  def resolvedStatusCode(url: String, cookie: Option[Cookie] = None): Try[Int] =
    Try {
      val http = Http(url)
        .method("GET")
        .option(followRedirects(true))
      cookie.fold(http)(c => http.cookie(c.name, c.value))
        .execute(sampleStream)
        .code
    }

  private def sampleStream(is: InputStream): Unit = {
    val pbis = new PushbackInputStream(is)
    val b1 = pbis.read()
    if (b1 == -1) throw new IllegalStateException("Could not read from input stream.") else pbis.unread(b1)
  }
}
