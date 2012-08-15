package net.shrine.webclient.server.api

import javax.ws.rs.ext.MessageBodyWriter
import java.lang.reflect.Type
import java.lang.annotation.{ Annotation => JAnnotation }
import javax.ws.rs.core.MediaType
import java.lang.reflect.ParameterizedType
import java.io.OutputStream
import java.io.OutputStreamWriter

/**
 * @author clint
 * @date Aug 6, 2012
 */
trait UnknownSizeMessageBodyWriter[T] extends MessageBodyWriter[T] {
  override def getSize(
    items: T,
    clazz: Class[_],
    genericType: Type,
    annotations: Array[JAnnotation],
    mediaType: MediaType): Long = -1L

  protected def isJson(mediaType: MediaType) = mediaType == MediaType.APPLICATION_JSON_TYPE
    
  protected def writeTo(out: OutputStream)(f: => String) {
    val writer = new OutputStreamWriter(out)

    try {
      writer.write(f)
    } finally {
      writer.flush()
    }
  }
}