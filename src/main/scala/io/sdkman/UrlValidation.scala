package io.sdkman

import scalaj.http.Http
import scalaj.http.HttpOptions.followRedirects

import scala.util.Try

trait UrlValidation {
  def hasOrphanedUrl(url: String, header: (String, String) = ("", "")): Boolean =
    Try {
      Http(url)
        .method("HEAD")
        .headers(header)
        .option(followRedirects(true))
        .asString
        .code
    }.fold(e => true, code => Seq(404, 403, 401).contains(code))
}
