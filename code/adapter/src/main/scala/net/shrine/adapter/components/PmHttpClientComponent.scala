package net.shrine.adapter.components

import net.shrine.util.HttpClient

/**
 * @author clint
 * @date Apr 5, 2013
 */
trait PmHttpClientComponent {
  val httpClient: HttpClient
  
  val pmEndpoint: String
  
  def callPm(payload: String) = httpClient.post(payload, pmEndpoint)
}