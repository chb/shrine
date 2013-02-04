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

  /**
   * Helpers for working with scala.util.Try
   */
  object Tries {
    /**
     * Implicits to allow mixing Options and Tries in for-comprehensions
     */
    object Implicits {

      implicit def option2Try[T](o: Option[T]): Try[T] = o match {
        case Some(value) => Success(value)
        case None => Failure(new Exception("None converted to Failure"))
      }

      implicit def try2Option[T](o: Try[T]): Option[T] = o.toOption
    }

    /**
     * Turns an Option[Try[T]] into a Try[Option[T]], a la Future.sequence.
     */
    def sequence[T](tryOption: Option[Try[T]]): Try[Option[T]] = tryOption match {
      case Some(attempt) => attempt.map(Option(_))
      case None => Try(None)
    }

    /**
     * Turns an Option[Traversable[T]] into a Try[Traversable[T]], a la Future.sequence.
     * Uses CanBuildFrom bagic to ensure that the subtype of Traversable passed in is the
     * same subtype of Traversable returned, and that this is verifiable at compile-time.
     * 
     * NB: If *any* of the input Tries are Failures, then the first Failure is returned;
     * this can drop subsequent Failures if there are more than one.
     */
    def sequence[A, C[+A] <: Traversable[A]](tries: C[Try[A]])(implicit cbf: CanBuildFrom[C[A], A, C[A]]): Try[C[A]] = {
      val firstFailure: Option[Failure[A]] = tries.find(_.isFailure).collect { case f: Failure[A] => f }

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