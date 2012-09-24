package net.shrine.protocol

import net.shrine.util.AsExtractor

/**
 * @author clint
 * 
 * Extractor to allow pattern matching on a ShrineRequest and extracting a RunQueryRequest
 * 
 * Patterned after scala.util.control.NonFatal: 
 * http://www.scala-lang.org/archives/downloads/distrib/files/nightly/docs/library/index.html#scala.util.control.NonFatal$
 */
object AsRunQueryRequest extends AsExtractor[ShrineRequest, RunQueryRequest]
