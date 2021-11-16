package io.sdkman

import com.typesafe.scalalogging.LazyLogging
import scalaj.http.Http
import scalaj.http.HttpOptions.followRedirects

import scala.util.{Failure, Success, Try}

trait UrlValidation {

  self: LazyLogging =>

  val connTimeout = 5000

  val readTimeout = 10000

  def resourceAvailable(url: String): Boolean =
    resolvedStatusCode(url) match {
      case Success(code) =>
        logger.info(s"URL $url responded with code: $code")
        !Seq(404, 403, 401, 500).contains(code)
      case Failure(e) =>
        logger.error(s"URL $url responded with ${e.getMessage}")
        false
    }

  def resolvedStatusCode(url: String): Try[Int] =
    Try {
      val response = Http(url)
        .method("HEAD")
        .option(followRedirects(true))
        .timeout(connTimeout, readTimeout)
        .asParamMap

      response.contentType match {
        case ct if isTextHtml(ct) =>
          throw new IllegalStateException("text/html content-type detected")
        case _ =>
          response.code
      }
    }

  private def isTextHtml(s: Option[String]): Boolean = s.exists(ct => ct.contains("text/html"))

}
