package net.shrine.adapter.spring

import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests
import org.springframework.test.context.ContextConfiguration

/**
 * @author clint
 * @date Apr 15, 2013
 * 
 * Abstract base class for test classes with injectable dependencies managed by Spring.
 * For convenience, mostly.  If this was a mixed-in trait, subclasses would have to 
 * repeat the ContextConfiguration annotation. 
 */
@ContextConfiguration(locations = Array("/testApplicationContext.xml"))
abstract class AbstractShrineJUnitSpringTest extends AbstractJUnit4SpringContextTests