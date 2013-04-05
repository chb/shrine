package net.shrine.adapter.components

import net.shrine.util.HttpClient

/**
 * @author clint
 * @date Apr 5, 2013
 */
final class MockHttpClient(toReturn: => String) extends HttpClient {
  var urlParam: Option[String] = None
  var inputParam: Option[String] = None

  override def post(input: String, url: String): String = {
    this.inputParam = Some(input)
    this.urlParam = Some(url)

    toReturn
  }
}