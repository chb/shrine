package net.shrine.webclient.server.api

import javax.ws.rs.ext.MessageBodyWriter
import java.util.{List => JList}
import java.lang.reflect.Type
import java.lang.annotation.{Annotation => JAnnotation}
import javax.ws.rs.core.MediaType
import java.lang.reflect.ParameterizedType
import javax.ws.rs.core.MultivaluedMap
import java.io.OutputStream

/**
 * @author clint
 * @date Aug 3, 2012
 */
abstract class JsonJavaListMessageBodyWriter[T : Manifest : Jsonable] extends MessageBodyWriter[JList[T]] {
  private val classOfT = manifest[T].erasure
  
  override def getSize(
    list: JList[T],
    clazz: Class[_],
    genericType: Type,
    annotations: Array[JAnnotation],
    mediaType: MediaType): Long = -1L

  override def isWriteable(
    clazz: Class[_],
    genericType: Type,
    annotations: Array[JAnnotation],
    mediaType: MediaType): Boolean = {

    val isAList = classOf[JList[_]].isAssignableFrom(clazz)

    val isAListOfT = genericType match {
      case parameterizedType: ParameterizedType => {
        val actualTypeArg = parameterizedType.getActualTypeArguments.head
        
        actualTypeArg == classOfT
      }
      case _ => false
    }
    
    isAList && isAListOfT && mediaType == MediaType.APPLICATION_JSON_TYPE
  }

  import scala.collection.JavaConverters._
  import net.liftweb.json._
  import net.liftweb.json.JsonDSL._
  
  override def writeTo(
    list: JList[T],
    clazz: Class[_],
    genericType: Type,
    annotations: Array[JAnnotation],
    mediaType: MediaType,
    map: MultivaluedMap[String, Object],
    out: OutputStream) {
    
    val toJson = implicitly[Jsonable[T]].toJson _
    
    val json = list.asScala.toSeq.map(toJson)
    
    val jsonString = compact(render(json))
    
    val writer = new java.io.OutputStreamWriter(out)
    
    try {
      writer.write(jsonString)
    } finally {
      writer.flush()
    }
  }
}