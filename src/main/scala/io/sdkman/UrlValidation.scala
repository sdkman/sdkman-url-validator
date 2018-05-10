package io.sdkman

import scalaj.http.Http
import scalaj.http.HttpOptions.followRedirects

import scala.util.Try

case class Cookie(name: String, value: String)

trait UrlValidation {
  def hasOrphanedUrl(url: String, cookie: Option[Cookie] = None): Boolean =
    resolvedStatusCode(url, cookie)
      .fold(e => true, code => Seq(404, 403, 401).contains(code))

  def resolvedStatusCode(url: String, cookie: Option[Cookie] = None): Try[Int] =
    Try {
      val http = Http(url)
        .method("HEAD")
        .option(followRedirects(true))
      cookie.fold(http)(c => http.cookie(c.name, c.value))
        .asString
        .code
    }
}
