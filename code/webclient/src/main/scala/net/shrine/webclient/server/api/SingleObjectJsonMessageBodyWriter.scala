package net.shrine.webclient.server.api

import java.io.OutputStream
import java.lang.annotation.{Annotation => JAnnotation}
import java.lang.reflect.Type

import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import net.liftweb.json.compact
import net.liftweb.json.render

/**
 * @author clint
 * @date Sep 4, 2012
 */
abstract class SingleObjectJsonMessageBodyWriter[T : Manifest : Jsonable] extends UnknownSizeMessageBodyWriter[T] {
  
  private val classOfT = manifest[T].erasure
  
  override def isWriteable(
    clazz: Class[_],
    genericType: Type,
    annotations: Array[JAnnotation],
    mediaType: MediaType): Boolean = {

    val isRightType = classOfT.isAssignableFrom(clazz)

    isRightType && isJson(mediaType)
  }

  override def writeTo(
    items: T,
    clazz: Class[_],
    genericType: Type,
    annotations: Array[JAnnotation],
    mediaType: MediaType,
    map: MultivaluedMap[String, Object],
    out: OutputStream) {

    val toJson = implicitly[Jsonable[T]].toJson _

    import net.liftweb.json._
    import net.liftweb.json.JsonDSL._

    val jsonString = compact(render(toJson(items)))

    writeTo(out)(jsonString)
  }
}