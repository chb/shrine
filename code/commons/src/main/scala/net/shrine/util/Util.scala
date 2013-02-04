package net.shrine.util

import javax.xml.datatype.XMLGregorianCalendar
import org.spin.tools.NetworkTime
import org.apache.log4j.Logger
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.collection.generic.CanBuildFrom
import scala.util.control.NonFatal

/**
 * @author clint
 * @date Oct 18, 2012
 */
object Util extends Loggable {

  //NB: Will use current locale
  def now: XMLGregorianCalendar = (new NetworkTime).getXMLGregorianCalendar

  def tryOrElse[T](default: => T)(f: => T): T = {
    try { f } catch {
      case NonFatal(e) => {
        error("Exception: ", e)

        default
      }
    }
  }

  object Tries {
    object Implicits {

      implicit def option2Try[T](o: Option[T]): Try[T] = o match {
        case Some(value) => Success(value)
        case None => Failure(new Exception("None converted to Failure"))
      }

      implicit def try2Option[T](o: Try[T]): Option[T] = o.toOption
    }

    def sequence[T](tryOption: Option[Try[T]]): Try[Option[T]] = tryOption match {
      case Some(attempt) => attempt.map(Option(_))
      case None => Try(None)
    }

    def sequence[T, C[T] <: Traversable[T]](tries: C[Try[T]])(implicit cbf: CanBuildFrom[C[T], T, C[T]]): Try[C[T]] = {
      val firstFailure: Option[Failure[T]] = tries.find(_.isFailure).collect { case f: Failure[T] => f }

      firstFailure match {
        case Some(failure) => failure.map(_ => cbf().result)
        //Otherwise, there are no failures
        case _ => Try {
          val builder = cbf()

          builder ++= tries.map(_.get)

          builder.result
        }
      }
    }
  }
}