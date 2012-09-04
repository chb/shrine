package net.shrine.webclient.server.api

import scala.{Iterable => SIterable}
import java.lang.reflect.Type
import java.lang.annotation.{Annotation => JAnnotation}
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import java.io.OutputStream
import java.lang.reflect.ParameterizedType
import javax.ws.rs.ext.MessageBodyWriter
import net.shrine.webclient.server.MultiInstitutionQueryResult
import javax.ws.rs.ext.Provider

/**
 * @author clint
 * @date Aug 6, 2012
 * 
 */
@Provider
final class MultiInstitutionQueryResultMessageBodyWriter extends SingleObjectJsonMessageBodyWriter[MultiInstitutionQueryResult]