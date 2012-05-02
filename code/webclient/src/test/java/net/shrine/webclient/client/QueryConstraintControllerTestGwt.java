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
		state.registerNewQuery(id("foo"), new Term("foo"));
		
		assertFalse(state.getQuery(id("foo")).isNegated());
		
		controller.setNegated(id("foo"), true);
		
		assertTrue(state.getQuery(id("foo")).isNegated());
		
		controller.setNegated(id("foo"), false);
		
		assertFalse(state.getQuery(id("foo")).isNegated());
		
		try {
			controller.setNegated(id("blah"), true);
			
			fail("Should have thrown when trying to mutate nonexistent query group");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testSetStartDate() {
		state.registerNewQuery(id("foo"), new Term("foo"));
		
		assertNull(state.getQuery(id("foo")).getStart());
		
		final Date date = new Date();
		
		controller.setStartDate(id("foo"), date);
		
		assertEquals(date, state.getQuery(id("foo")).getStart());
		
		try {
			controller.setStartDate(id("blah"), null);
			
			fail("Should have thrown when trying to mutate nonexistent query group");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testSetEndDate() {
		state.registerNewQuery(id("foo"), new Term("foo"));
		
		assertNull(state.getQuery(id("foo")).getEnd());
		
		final Date date = new Date();
		
		controller.setEndDate(id("foo"), date);
		
		assertEquals(date, state.getQuery(id("foo")).getEnd());
		
		try {
			controller.setEndDate(id("blah"), null);
			
			fail("Should have thrown when trying to mutate nonexistent query group");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testSetMinOccurs() {
		state.registerNewQuery(id("foo"), new Term("foo"));
		
		assertNull(state.getQuery(id("foo")).getEnd());
		
		final Date date = new Date();
		
		controller.setEndDate(id("foo"), date);
		
		assertEquals(date, state.getQuery(id("foo")).getEnd());
		
		try {
			controller.setEndDate(id("blah"), null);
			
			fail("Should have thrown when trying to mutate nonexistent query group");
		} catch(IllegalArgumentException expected) { }
	}
}
