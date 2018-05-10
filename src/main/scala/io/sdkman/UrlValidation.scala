package io.sdkman

import java.io.InputStream

import scalaj.http.Http
import scalaj.http.HttpOptions.followRedirects

import scala.io.Source
import scala.util.Try

case class Cookie(name: String, value: String)

trait UrlValidation {
  def hasOrphanedUrl(url: String, cookie: Option[Cookie] = None): Boolean =
    resolvedStatusCode(url, cookie)
      .fold(e => true, code => Seq(404, 403, 401).contains(code))

  def resolvedStatusCode(url: String, cookie: Option[Cookie] = None): Try[Int] =
    Try {
      val http = Http(url)
        .method("GET")
        .option(followRedirects(true))
      cookie.fold(http)(c => http.cookie(c.name, c.value))
        .execute(sampleStream)
        .code
    }

  private def sampleStream(is: InputStream) = {
    Source.fromInputStream(is).take(16)
  }
}
