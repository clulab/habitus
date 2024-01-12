package org.clulab.habitus.scraper.apps

import sttp.client4.quick._
import sttp.model.{Header, HeaderNames, MediaType, Uri}

object ScrapeSite extends App {
  val urlString = args.lift(0).getOrElse("https://www.adomonline.com/two-electrocuted-heavy-rains-sekondi-takoradi/")
  val response = quickRequest
      .get(Uri.unsafeParse(urlString))
      .header(Header(HeaderNames.UserAgent, "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0"))
      .contentType(MediaType.TextHtml)
      .send()
  val body = response.body

  println(body)
}
