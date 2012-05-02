package net.shrine.webclient.client;

import java.util.Date;

import net.shrine.webclient.client.domain.Term;

import org.junit.Test;

/**
 * 
 * @author clint
 * @date Apr 19, 2012
 */
public class QueryConstraintControllerTestGwt extends AbstractWebclientTest {

	private State state;
	
	private QueryConstraintController controller;
	
	@Override
	protected void gwtSetUp() throws Exception {
		super.gwtSetUp();
		
		state = new State();
		
		controller = new QueryConstraintController(state);
	}

	@Override
	protected void gwtTearDown() throws Exception {
		super.gwtTearDown();
		
		state = null;
		controller = null;
	}

	@Test
	public void testSetNegated() {
		state.registerNewQuery(new Term("foo"));
		
		assertFalse(state.getQuery(id("A")).isNegated());
		
		controller.setNegated(id("A"), true);
		
		assertTrue(state.getQuery(id("A")).isNegated());
		
		controller.setNegated(id("A"), false);
		
		assertFalse(state.getQuery(id("A")).isNegated());
		
		try {
			controller.setNegated(id("blah"), true);
			
			fail("Should have thrown when trying to mutate nonexistent query group");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testSetStartDate() {
		state.registerNewQuery(new Term("foo"));
		
		assertNull(state.getQuery(id("A")).getStart());
		
		final Date date = new Date();
		
		controller.setStartDate(id("A"), date);
		
		assertEquals(date, state.getQuery(id("A")).getStart());
		
		try {
			controller.setStartDate(id("blah"), null);
			
			fail("Should have thrown when trying to mutate nonexistent query group");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testSetEndDate() {
		state.registerNewQuery(new Term("foo"));
		
		assertNull(state.getQuery(id("A")).getEnd());
		
		final Date date = new Date();
		
		controller.setEndDate(id("A"), date);
		
		assertEquals(date, state.getQuery(id("A")).getEnd());
		
		try {
			controller.setEndDate(id("blah"), null);
			
			fail("Should have thrown when trying to mutate nonexistent query group");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testSetMinOccurs() {
		state.registerNewQuery(new Term("foo"));
		
		assertNull(state.getQuery(id("A")).getEnd());
		
		final Date date = new Date();
		
		controller.setEndDate(id("A"), date);
		
		assertEquals(date, state.getQuery(id("A")).getEnd());
		
		try {
			controller.setEndDate(id("sakljdkals"), null);
			
			fail("Should have thrown when trying to mutate nonexistent query group");
		} catch(IllegalArgumentException expected) { }
	}
}
