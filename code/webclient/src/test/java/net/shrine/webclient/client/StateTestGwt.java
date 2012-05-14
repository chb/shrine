package net.shrine.webclient.client;

import java.util.HashMap;

import net.shrine.webclient.client.domain.And;
import net.shrine.webclient.client.domain.IntWrapper;
import net.shrine.webclient.client.domain.QueryGroup;
import net.shrine.webclient.client.domain.Term;

import org.junit.Test;

/**
 * 
 * @author clint
 * @date Apr 19, 2012
 */
public class StateTestGwt extends AbstractWebclientTest {

	@Test
	public void testGuardQueryNameIsPresent() {
		final State state = new State();
		
		try {
			state.guardQueryIsPresent(12344);
			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }
		
		final int id = state.registerNewQuery(term("foo")).getId();
		
		state.guardQueryIsPresent(id);
	}

	@Test
	public void testNumQueryGroups() {
		final State state = new State();
		
		assertEquals(0, state.numQueryGroups());
		
		state.registerNewQuery(term("foo"));
		
		assertEquals(1, state.numQueryGroups());
		
		state.registerNewQuery(term("foo"));
		
		assertEquals(2, state.numQueryGroups());
	}

	@Test
	public void testCompleteAllQuery() {
		final State state = new State();
		
		@SuppressWarnings("serial")
		final HashMap<String, IntWrapper> results = new HashMap<String, IntWrapper>() {{
			this.put("foo", new IntWrapper(5));
			this.put("bar", new IntWrapper(99));
		}};
		
		assertTrue(state.getAllResult().isEmpty());
		
		state.completeAllQuery(results);
		
		assertTrue(state.getAllResult().isDefined());
		
		assertEquals(results, state.getAllResult().get());
	}

	@Test
	public void testRegisterNewQuery() {
		final State state = new State();
		
		final Term expr = term("foo");
		
		final int id = state.registerNewQuery(expr).getId();
		
		assertTrue(state.isQueryIdPresent(id));
		
		final QueryGroup group = state.getQuery(id);
		
		assertNotNull(group);
		
		assertEquals(expr, group.getExpression());
		assertNull(group.getStart());
		assertNull(group.getEnd());
		assertEquals(1, group.getMinOccurances());
		assertFalse(group.isNegated());
	}

	@Test
	public void testUpdateAllExpression() {
		final State state = new State();
		
		final Term t1 = term("foo");
		final Term t2 = term("bar");
		
		try {
			state.updateAllExpression();
			
			fail("Should have thrown with no query groups");
		} catch(IllegalArgumentException expected) { }
		
		state.registerNewQuery(t1);
		
		state.updateAllExpression();
		
		assertEquals(t1.toXmlString(), state.getAllExpression());
		
		state.registerNewQuery(t2);
		
		state.updateAllExpression();
		
		assertEquals(new And(t1, t2).toXmlString(), state.getAllExpression());
	}
}
