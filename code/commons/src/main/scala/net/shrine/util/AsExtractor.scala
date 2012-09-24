package net.shrine.util

/**
 * @author clint
 * @date Sep 20, 2012
 *
 * Generic extractor base class.  Allows matching against an S to extract/coerce/cast it into a T.
 * For example:
 *
 * object AsException extends AsExtractor[Throwable, Exception]
 *
 * val t: Throwable = ...
 *
 * val AsException(e) = t
 *
 * this matches Exceptions (and Exception subclasses, but not other Throwables, say, Errors.  Further,
 * If the match succeeds, the variable e is available and has type Exception, not Throwable.
 * 
 * Idea taken from scala.util.control.NonFatal
 */
abstract class AsExtractor[S, T <: S: Manifest] {
  /**
   * A predicate method: Allows testing is an S is actually an instance of T.  For example: 
   * 
   * object AsException extends AsExtractor[Throwable, Exception]
   * 
   * case foo if AsException(foo) => ... //foo is an Exception
   */
  def apply(thing: S) = isAT(thing)

  private def isAT(thing: S) = manifest[T].erasure.isInstance(thing)

  /**
   * Extractor method: Allows matching against an S to extract/coerce/cast it into a T.  For example:
   *
   * object AsException extends AsExtractor[Throwable, Exception]
   *
   * val t: Throwable = ...
   *
   * val AsException(e) = t
   *
   * this matches Exceptions (and Exception subclasses, but not other Throwables, say, Errors.  Further,
   * If the match succeeds, the variable e is available and has type Exception, not Throwable.
   */
  def unapply(thing: S): Option[T] = {
    Option(thing).collect { case actuallyIsAT: T if isAT(actuallyIsAT) => actuallyIsAT }
  }
}