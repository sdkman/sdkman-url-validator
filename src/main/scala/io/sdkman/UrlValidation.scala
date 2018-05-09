package io.sdkman

import scalaj.http.Http
import scalaj.http.HttpOptions.followRedirects

import scala.util.Try

trait UrlValidation {
  def hasOrphanedUrl(url: String, cookie: Option[(String, String)] = None): Boolean =
    Try {
      val http = Http(url)
        .method("HEAD")
        .option(followRedirects(true))
      cookie.fold(http)(c => http.cookie(c._1, c._2))
        .asString
        .code
    }.fold(e => true, code => Seq(404, 403, 401).contains(code))
}
