package net.shrine.util

import com.sun.jersey.test.framework.JerseyTest
import com.sun.jersey.test.framework.AppDescriptor
import com.sun.jersey.api.core.ResourceConfig
import com.sun.jersey.api.core.ClassNamesResourceConfig
import com.sun.jersey.spi.inject.InjectableProvider
import java.lang.reflect.Type
import com.sun.jersey.core.spi.component.ComponentScope
import com.sun.jersey.core.spi.component.ComponentContext
import com.sun.jersey.spi.inject.Injectable
import com.sun.jersey.test.framework.LowLevelAppDescriptor.Builder

/**
 * @author clint
 * @date Apr 10, 2013
 */
final class JerseyAppDescriptor[T: Manifest] {
  import net.shrine.service.annotation

  //Make an appdescriptor that will inject the passed object when supplying values for parameters annotated with
  //net.shrine.service.annotation.Requesthandler
  def using(instance: => AnyRef): AppDescriptor = makeAppDescriptor { (context, a, t) =>
    new Injectable[AnyRef] {
      def getValue: AnyRef = instance
    }
  }

  //Make an appdescriptor that will inject the passed object with the right type when supplying values for 
  //parameters annotated with net.shrine.service.annotation.Requesthandler
  def using(injectableParams: (Type, AnyRef)*): AppDescriptor = makeAppDescriptor { (context, a, t) =>
    val injectableParamsByType = injectableParams.toMap
    
    new Injectable[AnyRef] {
      //NB: Intentionally fail loudly on an unknown dependency type
      def getValue: AnyRef = injectableParamsByType(t) 
    }
  }

  private def makeAppDescriptor(createInjectable: (ComponentContext, annotation.RequestHandler, Type) => Injectable[AnyRef]): AppDescriptor = {
    //Make a ResourceConfig that describes the one class - ShrineResource - we want Jersey to instantiate and expose 
    val resourceConfig: ResourceConfig = new ClassNamesResourceConfig(manifest[T].runtimeClass)

    //Register an InjectableProvider that produces a mock ShrineRequestHandler that will be provided to the ShrineResource
    //instantiated by Jersey.  For this to work, the ShrineRequestHandler constructor param on ShrineResource must be 
    //annotated with @net.shrine.service.annotation.ShrineRequestHandler.  Here, we map that annotation to an 
    //InjectableProvider that produces mock ShrineRequestHandlers and register this with the ResourceConfig.
    import net.shrine.service.annotation

    resourceConfig.getSingletons.add(new InjectableProvider[annotation.RequestHandler, Type] {
      override def getScope = ComponentScope.Singleton

      override def getInjectable(context: ComponentContext, a: annotation.RequestHandler, t: Type) = createInjectable(context, a, t)
    })

    //Make an AppDescriptor from the ResourceConfig
    new Builder(resourceConfig).build
  }
}

object JerseyAppDescriptor {
  def forResource[T: Manifest] = new JerseyAppDescriptor[T]
}