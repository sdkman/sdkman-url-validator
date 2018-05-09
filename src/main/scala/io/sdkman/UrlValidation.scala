package io.sdkman

import scalaj.http.Http
import scalaj.http.HttpOptions.followRedirects

import scala.util.Try

trait UrlValidation {
  def hasOrphanedUrl(url: String, header: Option[(String, String)] = None): Boolean =
    Try {
      val http = Http(url)
        .method("HEAD")
        .option(followRedirects(true))
      header.fold(http)(h => http.headers(h))
        .asString
        .code
    }.fold(e => true, code => Seq(404, 403, 401).contains(code))
}
