package net.shrine.webclient.client;

import static org.junit.Assert.fail;

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
		state = new State();
		
		controller = new QueryConstraintController(state);
	}

	@Override
	protected void gwtTearDown() throws Exception {
		state = null;
		controller = null;
	}

	@Test
	public void testSetNegated() {
		state.registerNewQuery("foo", new Term("foo"));
		
		assertFalse(state.getQueries().get("foo").isNegated());
		
		controller.setNegated("foo", true);
		
		assertTrue(state.getQueries().get("foo").isNegated());
		
		controller.setNegated("foo", false);
		
		assertFalse(state.getQueries().get("foo").isNegated());
		
		try {
			controller.setNegated("blah", true);
			
			fail("Should have thrown when trying to mutate nonexistent query group");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testSetStartDate() {
		state.registerNewQuery("foo", new Term("foo"));
		
		assertNull(state.getQueries().get("foo").getStart());
		
		final Date date = new Date();
		
		controller.setStartDate("foo", date);
		
		assertEquals(date, state.getQueries().get("foo").getStart());
		
		try {
			controller.setStartDate("blah", null);
			
			fail("Should have thrown when trying to mutate nonexistent query group");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testSetEndDate() {
		state.registerNewQuery("foo", new Term("foo"));
		
		assertNull(state.getQueries().get("foo").getEnd());
		
		final Date date = new Date();
		
		controller.setEndDate("foo", date);
		
		assertEquals(date, state.getQueries().get("foo").getEnd());
		
		try {
			controller.setEndDate("blah", null);
			
			fail("Should have thrown when trying to mutate nonexistent query group");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testSetMinOccurs() {
		state.registerNewQuery("foo", new Term("foo"));
		
		assertNull(state.getQueries().get("foo").getEnd());
		
		final Date date = new Date();
		
		controller.setEndDate("foo", date);
		
		assertEquals(date, state.getQueries().get("foo").getEnd());
		
		try {
			controller.setEndDate("blah", null);
			
			fail("Should have thrown when trying to mutate nonexistent query group");
		} catch(IllegalArgumentException expected) { }
	}
}
