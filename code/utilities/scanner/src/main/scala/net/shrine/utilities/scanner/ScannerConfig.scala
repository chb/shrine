package net.shrine.utilities.scanner

import scala.concurrent.duration.Duration
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigException
import scala.util.Try

/**
 * @author clint
 * @date Mar 6, 2013
 */
final case class ScannerConfig(
  val adapterMappingsFile: String,
  val ontologySqlFile: String,
  val reScanTimeout: Duration,
  val shrineUrl: String,
  val projectId: String,
  val authorization: AuthenticationInfo)

object ScannerConfig {
  private[scanner] def getReScanTimeout(subConfig: Config): Duration = {
    import scala.concurrent.duration._
    import Keys._

    if (subConfig.hasPath(milliseconds)) { subConfig.getLong(milliseconds).milliseconds }
    else if (subConfig.hasPath(seconds)) { subConfig.getLong(seconds).seconds }
    else if (subConfig.hasPath(minutes)) { subConfig.getLong(minutes).minutes }
    else { throw new ConfigException.Missing(s"Expected to find one of scanner.reScanTimeout.{${milliseconds}, ${seconds}, ${minutes}} at subConfig") }
  }

  def apply(config: Config): ScannerConfig = {
    import Keys._

    def getAuthInfo(subConfig: Config): AuthenticationInfo = {
      def requirePath(path: String) = if (!subConfig.hasPath(path)) throw new ConfigException.Missing(s"Expected to find '$path' in $subConfig")

      requirePath(domain)
      requirePath(username)
      requirePath(password)

      AuthenticationInfo(subConfig.getString(domain), subConfig.getString(username), Credential(subConfig.getString(password), false))
    }

    ScannerConfig(
      config.getString(adapterMappingsFile),
      config.getString(ontologySqlFile),
      getReScanTimeout(config.getConfig(reScanTimeout)),
      config.getString(shrineUrl),
      config.getString(projectId),
      getAuthInfo(config.getConfig(credentials)))
  }

  object Keys {
    val minutes = "minutes"
    val seconds = "seconds"
    val milliseconds = "milliseconds"

    val domain = "domain"
    val username = "username"
    val password = "password"

    private def subKey(k: String) = s"scanner.$k"

    val adapterMappingsFile = subKey("adapterMappingsFile")
    val ontologySqlFile = subKey("ontologySqlFile")
    val reScanTimeout = subKey("reScanTimeout")
    val shrineUrl = subKey("shrineUrl")
    val projectId = subKey("projectId")
    val credentials = subKey("credentials")
  }
}
