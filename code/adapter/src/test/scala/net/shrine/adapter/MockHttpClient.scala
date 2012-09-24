package net.shrine.adapter

import net.shrine.util.HttpClient

/**
 * @author clint
 * @date Sep 20, 2012
 */
object MockHttpClient extends HttpClient {
  override def post(input: String, url: String): String = ""
    
  def apply(f: => String) = new HttpClient {
    override def post(input: String, url: String): String = f
  }    
}