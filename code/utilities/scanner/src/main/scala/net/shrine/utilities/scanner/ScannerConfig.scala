package net.shrine.utilities.scanner

import scala.concurrent.duration.Duration
import net.shrine.protocol.AuthenticationInfo

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