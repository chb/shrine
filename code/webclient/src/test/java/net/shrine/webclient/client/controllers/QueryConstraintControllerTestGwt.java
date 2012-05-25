package net.shrine.webclient.client.controllers;

import java.util.Date;

import net.shrine.webclient.client.AbstractWebclientTest;
import net.shrine.webclient.client.state.State;

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
		
		state = state();
		
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
		final int fooId = state.registerNewQuery(term("foo")).getId();
		
		assertFalse(state.getQuery(fooId).isNegated());
		
		controller.setNegated(fooId, true);
		
		assertTrue(state.getQuery(fooId).isNegated());
		
		controller.setNegated(fooId, false);
		
		assertFalse(state.getQuery(fooId).isNegated());
		
		try {
			controller.setNegated(12345, true);
			
			fail("Should have thrown when trying to mutate nonexistent query group");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testSetStartDate() {
		final int fooId = state.registerNewQuery(term("foo")).getId();
		
		assertNull(state.getQuery(fooId).getStart());
		
		final Date date = new Date();
		
		controller.setStartDate(fooId, date);
		
		assertEquals(date, state.getQuery(fooId).getStart());
		
		try {
			controller.setStartDate(42, null);
			
			fail("Should have thrown when trying to mutate nonexistent query group");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testSetEndDate() {
		final int fooId = state.registerNewQuery(term("foo")).getId();
		
		assertNull(state.getQuery(fooId).getEnd());
		
		final Date date = new Date();
		
		controller.setEndDate(fooId, date);
		
		assertEquals(date, state.getQuery(fooId).getEnd());
		
		try {
			controller.setEndDate(98765, null);
			
			fail("Should have thrown when trying to mutate nonexistent query group");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testSetMinOccurs() {
		final int fooId = state.registerNewQuery(term("foo")).getId();
		
		assertNull(state.getQuery(fooId).getEnd());
		
		final Date date = new Date();
		
		controller.setEndDate(fooId, date);
		
		assertEquals(date, state.getQuery(fooId).getEnd());
		
		try {
			controller.setEndDate(89746, null);
			
			fail("Should have thrown when trying to mutate nonexistent query group");
		} catch(IllegalArgumentException expected) { }
	}
}
