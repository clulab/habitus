package org.clulab.habitus.scraper.apps

import sttp.client4.quick._
import sttp.model.{Header, HeaderNames, MediaType, Uri}

object ScrapeSiteApp extends App {
  // This requires Java 11.
  val urlString = args.lift(0).getOrElse("https://www.adomonline.com/cctv-captures-2-allegedly-stealing-gh%e2%82%b5-205910/")
  val response = quickRequest
      .get(Uri.unsafeParse(urlString))
      .header(Header(HeaderNames.UserAgent, "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0"))
      .contentType(MediaType.TextHtml)
      .send()
  val body = response.body

  println(body)
}
