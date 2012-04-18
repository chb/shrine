package net.shrine.webclient.server

import net.shrine.webclient.client.GreetingService
//import net.shrine.webclient.shared.FieldVerifier
import com.google.gwt.user.server.rpc.RemoteServiceServlet

/**
 * The server side implementation of the RPC service.
 */
final class GreetingServiceImpl extends RemoteServiceServlet with GreetingService {

  override def greetServer(input: String): String = {
    // Verify that the input is valid.
    //require(FieldVerifier.isValidName(input), "Name must be at least 4 characters long")

    val serverInfo = getServletContext.getServerInfo
    val userAgent = escapeHtml(getThreadLocalRequest.getHeader("User-Agent"))

    // Escape data from the client to avoid cross-site script vulnerabilities.

    println("Greeting '" + escapeHtml(input) + "'")
    
    "Hello, " + escapeHtml(input) + "!<br><br>I am running " + serverInfo + ".<br><br>It looks like you are using:<br>" + userAgent
  }

  /**
   * Escape an html string. Escaping data received from the client helps to
   * prevent cross-site script vulnerabilities.
   *
   * @param html the html string to escape
   * @return the escaped string
   */
  private def escapeHtml(html: String): String = {
    Option(html).map(_.replaceAll("&", "&amp").replaceAll("<", "&lt").replaceAll(">", "&gt")).orNull
  }
}
