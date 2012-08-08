package net.shrine.webclient.server.api

import com.sun.jersey.test.framework.JerseyTest
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.matchers.ShouldMatchers
import com.sun.jersey.test.framework.AppDescriptor
import com.sun.jersey.api.core.ResourceConfig
import com.sun.jersey.api.core.ClassNamesResourceConfig
import com.sun.jersey.spi.inject.InjectableProvider
import java.lang.reflect.Type
import com.sun.jersey.core.spi.component.ComponentScope
import com.sun.jersey.core.spi.component.ComponentContext
import net.shrine.webclient.server.QueryService
import net.shrine.webclient.server.QueryServiceImpl
import net.shrine.webclient.server.OntologyService
import net.shrine.webclient.server.OntologyServiceImplTest
import net.shrine.webclient.server.OntologyServiceImpl
import com.sun.jersey.test.framework.LowLevelAppDescriptor.Builder

trait ShrineWebclientApiJaxrsTest extends AssertionsForJUnit with ShouldMatchers { self: JerseyTest =>
  
  def queryService: QueryService
  
  def ontologyService: OntologyService
  
  /**
   * We invoked the no-arg superclass constructor, so we must override configure() to provide an AppDescriptor
   * That tells Jersey to instantiate and expose ClientApiResource
   */
  override def configure: AppDescriptor = {
    //Make a ResourceConfig that describes the one class - ClientApiResource - we want Jersey to instantiate and expose.
    //Also reference needed MessageBodyWriters
    val resourceConfig: ResourceConfig = new ClassNamesResourceConfig(classOf[ClientApiResource],
        															   classOf[MultiInstitutionQueryResultMessageBodyWriter],
        															   classOf[OntNodeSeqMessageBodyWriter],
        															   classOf[SuggestionSeqMessageBodyWriter])

    //Register an InjectableProvider that produces mock QueryServices and OntologyServices
    //that will be provided to the ClientApiResource instantiated by Jersey.  For this to work, 
    //the QueryService and OntologyService constructor params on ClientApiResource must be 
    //annotated with @net.shrine.webclient.server.Injectable.  Here, we map that annotation to an 
    //InjectableProvider that produces mock QueryServices and OntologyServices - based on the type
    //requested - and register this with the ResourceConfig. 
    resourceConfig.getSingletons.add(new InjectableProvider[Injectable, Type] {
      override def getScope: ComponentScope = ComponentScope.Singleton

      override def getInjectable(context: ComponentContext, a: Injectable, t: Type) = new com.sun.jersey.spi.inject.Injectable[AnyRef] {
        def getValue: AnyRef = t match {
          case _ if t == classOf[QueryService] => queryService
          case _ if t == classOf[OntologyService] => ontologyService
          case _ => sys.error("Unknown type: " + t)
        }
      }
    })

    //Make an AppDescriptor from the ResourceConfig
    new Builder(resourceConfig).build
  }
  
  import net.liftweb.json._
  
  protected def asJsonArray(rawJson: String): Option[JArray] = Option(parse(rawJson)) collect { case a: JArray => a }
  
  protected def unmarshalSeq[T : Jsonable](rawJson: String): Seq[T] = {
    val items = asJsonArray(rawJson).map(_.children).flatten
    
    items.flatMap(unmarshal[T](_)).toSeq
  }
  
  protected def unmarshal[T : Jsonable](json: String): Option[T] = unmarshal[T](parse(json))
  
  protected def unmarshal[T : Jsonable](json: JValue): Option[T] = implicitly[Jsonable[T]].fromJson(json)
  
  protected def ontResource = resource.path("api/ontology")
  
  protected def queryResource = resource.path("api/query")
  
  import OntologyServiceImpl.toConcept
  
  protected val gender = toConcept("""\\SHRINE\SHRINE\Demographics\Gender\""")
  
  protected val male = toConcept("""\\SHRINE\SHRINE\Demographics\Gender\Male\""")
  protected val female = toConcept("""\\SHRINE\SHRINE\Demographics\Gender\Female\""")
  protected val undifferentiated = toConcept("""\\SHRINE\SHRINE\Demographics\Gender\Undifferentiated\""")
  protected val unknown = toConcept("""\\SHRINE\SHRINE\Demographics\Gender\Unknown\""")
}