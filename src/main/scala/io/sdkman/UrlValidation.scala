package io.sdkman

import java.io.{InputStream, PushbackInputStream}

import com.typesafe.scalalogging.LazyLogging
import scalaj.http.Http
import scalaj.http.HttpOptions.followRedirects

import scala.util.{Failure, Success, Try}

case class Cookie(name: String, value: String)

trait UrlValidation {

  self: LazyLogging =>

  val connTimeout = 5000

  val readTimeout = 10000

  def resourceAvailable(url: String, cookie: Option[Cookie] = None): Boolean =
    resolvedStatusCode(url, cookie) match {
      case Success(code) =>
        logger.info(s"URL $url responded with code: $code")
        !Seq(404, 403, 401, 500).contains(code)
      case Failure(e) =>
        logger.error(s"URL $url responded with ${e.getMessage}")
        false
    }

  def resolvedStatusCode(url: String, cookie: Option[Cookie] = None): Try[Int] =
    Try {
      val http = Http(url)
        .method("GET")
        .option(followRedirects(true))
        .timeout(connTimeout, readTimeout)

      val response = cookie
        .fold(http)(c => http.cookie(c.name, c.value))
        .execute(sampleStream)

      response.contentType match {
        case ct if isTextHtml(ct) =>
          throw new IllegalStateException("text/html content-type detected")
        case _ =>
          response.code
      }
    }

  private def isTextHtml(s: Option[String]): Boolean = s.exists(ct => ct.contains("text/html"))

  private def sampleStream(is: InputStream): Unit = {
    val pbis = new PushbackInputStream(is)
    val b1 = pbis.read()
    if (b1 == -1) throw new IllegalStateException("Could not read from input stream.") else pbis.unread(b1)
  }
}
