package net.shrine.webclient.server.api

import java.util.{ Set => JSet }
import java.util.{ HashSet => JHashSet }
import scala.collection.JavaConverters._
import javax.ws.rs.core.Application
import javax.ws.rs.ApplicationPath
import javax.ws.rs.Path

//:(
@Path("rest")
@ApplicationPath("rest")
final class ClientApiApplication extends Application {
  override def getClasses: JSet[Class[_]] = jSet(classOf[ClientApiResource])

  override def getSingletons: JSet[AnyRef] = jSet(new ClientApiResource)
  
  private def jSet[T](thing: T): JSet[T] = {
    val result: JSet[T] = new JHashSet[T]
    
    result.add(thing)
    
    result
  }
}
