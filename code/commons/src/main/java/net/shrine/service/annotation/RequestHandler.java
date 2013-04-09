package net.shrine.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Clint Gilbert
 * @date Sep 15, 2011
 *
 * @link http://cbmi.med.harvard.edu
 * 
 * This software is licensed under the LGPL
 * @link http://www.gnu.org/licenses/lgpl.html
 * 
 * Custom annotation to indicate parameters that should be managed and injected by JAXRS, instead of Spring.  
 * For use with tests like ShrineResourceJaxrsTest, which must inject mock delegates into JAXRS root resource 
 * classes under test.
 * 
 * JAXRS allows mapping annotations to InjectionProviders which are asked to provide objects when parameters 
 * the mapped annotation are encountered.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestHandler { }
