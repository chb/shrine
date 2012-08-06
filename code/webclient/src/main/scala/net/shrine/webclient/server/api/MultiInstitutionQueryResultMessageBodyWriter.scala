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
final class MultiInstitutionQueryResultMessageBodyWriter extends UnknownSizeMessageBodyWriter[MultiInstitutionQueryResult] {

  override def isWriteable(
    clazz: Class[_],
    genericType: Type,
    annotations: Array[JAnnotation],
    mediaType: MediaType): Boolean = {
    
    val isRightType = classOf[MultiInstitutionQueryResult].isAssignableFrom(clazz)
    
    isRightType && isJson(mediaType)
  }
  
  override def writeTo(
    items: MultiInstitutionQueryResult,
    clazz: Class[_],
    genericType: Type,
    annotations: Array[JAnnotation],
    mediaType: MediaType,
    map: MultivaluedMap[String, Object],
    out: OutputStream) {
    
    val toJson = Jsonable.multiInstitutionQueryResultIsJsonable.toJson _

    import net.liftweb.json._
    import net.liftweb.json.JsonDSL._
    
    val jsonString = compact(render(toJson(items)))
    
    writeTo(out)(jsonString)
  }
}