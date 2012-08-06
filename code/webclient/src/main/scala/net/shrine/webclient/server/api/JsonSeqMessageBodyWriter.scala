package net.shrine.webclient.server.api

import javax.ws.rs.ext.MessageBodyWriter
import java.lang.reflect.Type
import java.lang.annotation.{ Annotation => JAnnotation }
import javax.ws.rs.core.MediaType
import java.lang.reflect.ParameterizedType
import javax.ws.rs.core.MultivaluedMap
import java.io.OutputStream

/**
 * @author clint
 * @date Aug 3, 2012
 */
abstract class JsonSeqMessageBodyWriter[T : Manifest : Jsonable] extends UnknownSizeMessageBodyWriter[Seq[T]] { 

  private val classOfT = manifest[T].erasure 
  
  override def isWriteable(
    clazz: Class[_],
    genericType: Type,
    annotations: Array[JAnnotation],
    mediaType: MediaType): Boolean = {

    val isAcceptableContainer = classOf[Seq[_]].isAssignableFrom(clazz)

    val isContainerOfT = genericType match {
      case parameterizedType: ParameterizedType => {
        val actualTypeArg = parameterizedType.getActualTypeArguments.head

        actualTypeArg == classOfT
      }
      case _ => false
    }

    isAcceptableContainer && isContainerOfT && isJson(mediaType)
  }
  
  override def writeTo(
    items: Seq[T],
    clazz: Class[_],
    genericType: Type,
    annotations: Array[JAnnotation],
    mediaType: MediaType,
    map: MultivaluedMap[String, Object],
    out: OutputStream) {

    val toJson = implicitly[Jsonable[T]].toJson _

    import net.liftweb.json._
    import net.liftweb.json.JsonDSL._
    
    val jsonString = compact(render(items.map(toJson)))

    writeTo(out)(jsonString)
  }
}