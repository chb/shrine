package net.shrine.utilities.scanner

import org.rogach.scallop.ScallopConf
import scala.concurrent.duration.Duration
import org.rogach.scallop.ValueConverter
import org.rogach.scallop.ArgType
import scala.util.Try
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import scala.reflect.runtime.universe._
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.rogach.scallop.Scallop
import java.util.concurrent.TimeUnit

/**
 * @author clint
 * @date Mar 26, 2013
 */
final case class CommandLineScannerConfig(arguments: Seq[String]) extends ScallopConf(arguments) {
  override protected def onError(e: Throwable) = CommandLineScannerConfig.onError(e, builder)

  import CommandLineScannerConfig._

  val adapterMappingsFile = opt[String](required = false)
  val ontologySqlFile = opt[String](short = 's', required = false)
  val rescanTimeout = opt[Duration](short = 't', required = false)
  val projectId = opt[String](required = false)
  val credentials = opt[AuthenticationInfo](required = false)
  val url = opt[String](required = false)
  val outputFile = opt[String](short = 'o', required = false)
  val showVersionToggle = toggle("version")
  val showHelpToggle = toggle("help")

  def toTypesafeConfig: Config = {
    import scala.collection.JavaConverters._
    import ScannerConfig.Keys

    def credentialKey(k: String) = s"${Keys.credentials}.$k"
    def durationKey(k: String) = s"${Keys.reScanTimeout}.$k"

    val exceptTimeout = Map(
      Keys.adapterMappingsFile -> adapterMappingsFile.get,
      Keys.ontologySqlFile -> ontologySqlFile.get,
      Keys.projectId -> projectId.get,
      Keys.shrineUrl -> url.get,
      Keys.outputFile -> outputFile.get,
      credentialKey(Keys.domain) -> credentials.get.map(_.domain),
      credentialKey(Keys.username) -> credentials.get.map(_.username),
      credentialKey(Keys.password) -> credentials.get.map(_.credential.value))

    val timeoutTupleOption = rescanTimeout.get.map(d => durationKey(timeUnitsToNames(d.unit)) -> Some(d.length))
      
    val withTimeout = exceptTimeout ++ timeoutTupleOption

    ConfigFactory.parseMap(withTimeout.collect { case (k, Some(v)) => (k, v) }.asJava)
  }
}

object CommandLineScannerConfig {
  private val timeUnitsToNames = {
    import TimeUnit._
    import ScannerConfig.Keys
    
    Map(MILLISECONDS -> Keys.milliseconds, SECONDS -> Keys.seconds, MINUTES -> Keys.minutes)
  }
  
  private def onError(e: Throwable, scallop: Scallop): String = {
    //println("Error: %s".format(e.getMessage))

    //scallop.printHelp

    ""
  }

  type Parser[A] = PartialFunction[(String, List[String]), Either[Unit, Option[A]]]

  private implicit val durationValueConverter: ValueConverter[Duration] = new ManifestValueConverter[Duration] {
    override val parseFirst: Parser[Duration] = {
      case (_, Seq(howManyString, timeUnitString)) => Right {
        Try(howManyString.toInt).flatMap(durationFrom(_, timeUnitString)).toOption
      }
    }
  }

  private implicit val authnValueConverter: ValueConverter[AuthenticationInfo] = new ManifestValueConverter[AuthenticationInfo] {
    override val parseFirst: Parser[AuthenticationInfo] = {
      case (_, Seq(domain, username, password)) => Right {
        Option(authInfoFrom(domain, username, password))
      }
    }
  }

  private abstract class ManifestValueConverter[A: TypeTag] extends ValueConverter[A] {
    val parseFirst: Parser[A]

    override def parse(s: List[(String, List[String])]): Either[Unit, Option[A]] = {
      s.headOption.collect(parseFirst).getOrElse(Left(()))
    }

    override val tag: TypeTag[A] = typeTag[A]

    /** Type of parsed argument list. */
    override val argType: ArgType.V = ArgType.LIST
  }

  private[scanner] def durationFrom(magnitude: Int, timeUnit: String): Try[Duration] = Try {
    import scala.concurrent.duration._

    timeUnit match {
      case ScannerConfig.Keys.`milliseconds` => magnitude.milliseconds
      case ScannerConfig.Keys.`seconds` => magnitude.seconds
      case ScannerConfig.Keys.`minutes` => magnitude.minutes
      case _ => throw new IllegalArgumentException(s"Unhandled time unit '$timeUnit'")
    }
  }

  private[scanner] def authInfoFrom(domain: String, username: String, password: String): AuthenticationInfo = {
    AuthenticationInfo(domain, username, Credential(password, false))
  }
}
