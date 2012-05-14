package net.shrine.webclient.client;

import net.shrine.webclient.client.domain.Or;
import net.shrine.webclient.client.domain.QueryGroup;
import net.shrine.webclient.client.domain.Term;

import org.junit.Test;

/**
 * 
 * @author clint
 * @date Apr 19, 2012
 */
public class QueryBuildingControllerTestGwt extends AbstractWebclientTest {

	@Test
	public void testMoveTerm() {
		fail("TODO");
	}
	
	@Test
	public void testAddNewTerm() {
		final State state = new State();
		
		final QueryBuildingController controller = new QueryBuildingController(state);
		
		final Term term1 = term("foo");
		final Term term2 = term("bar");
		
		assertEquals(0, state.numQueryGroups());
		
		final int t1id = controller.addNewTerm(term1).getId();
		
		assertEquals(1, state.numQueryGroups());
		
		final QueryGroup group = state.getQuery(t1id);
		
		assertEquals(term1, group.getExpression());
		
		final int t2id = controller.addNewTerm(term2).getId();
		
		assertEquals(2, state.numQueryGroups());
		
		assertEquals(term1, state.getQuery(t1id).getExpression());
		assertEquals(term2, state.getQuery(t2id).getExpression());
	}
	
	public void testRemoveAllQueryGroups() {
		final State state = new State();
		
		final QueryBuildingController controller = new QueryBuildingController(state);
		
		assertEquals(0, state.numQueryGroups());
		
		controller.removeAllQueryGroups();
		
		assertEquals(0, state.numQueryGroups());
		
		state.registerNewQuery(term("foo"));
		state.registerNewQuery(term("blah"));
		
		assertEquals(2, state.numQueryGroups());
		
		controller.removeAllQueryGroups();
		
		assertEquals(0, state.numQueryGroups());
	}
	
	public void testRemoveQueryGroup() {
		final State state = new State();
		
		final QueryBuildingController controller = new QueryBuildingController(state);
		
		final int fooId = state.registerNewQuery(term("foo")).getId();
		final int barId = state.registerNewQuery(term("blah")).getId();
		
		assertEquals(2, state.numQueryGroups());
		
		try {
			controller.removeQueryGroup(9483);
			
			fail("should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		controller.removeQueryGroup(fooId);
		
		assertEquals(1, state.numQueryGroups());
		
		try {
			state.guardQueryIsPresent(fooId);
			
			fail("should have thrown");
		} catch (IllegalArgumentException expected) { }
		
		state.guardQueryIsPresent(barId);
		
		controller.removeQueryGroup(barId);
		
		assertEquals(0, state.numQueryGroups());
	}
	
	public void testRemoveTerm() {
		final State state = new State();
		
		final QueryBuildingController controller = new QueryBuildingController(state);
		
		final Term t1 = term("foo");
		final Term t2 = term("blah");
		final Term t3 = term("nuh");
		final Term t4 = term("zuh");
		final Or or = new Or(t2, t3, t4);
		
		final int t1Id = state.registerNewQuery(t1).getId();
		
		final int orId = state.registerNewQuery(or).getId();
		
		assertEquals(2, state.numQueryGroups());
		
		try {
			controller.removeTerm(12345, t1);
			
			fail("should have thrown on unknown query group");
		} catch(IllegalArgumentException expected) { }
		
		//Try to remove term that's not part of this query group - shouldn't do anything
		controller.removeTerm(t1Id, t2);
		
		assertEquals(2, state.numQueryGroups());
		
		assertEquals(t1, state.getQuery(t1Id).getExpression());
		assertEquals(or, state.getQuery(orId).getExpression());
		
		//Remove term that's actually present
		
		//Single-term expressions should be removed
		controller.removeTerm(t1Id, t1);
		
		assertEquals(1, state.numQueryGroups());
		assertEquals(or, state.getQuery(orId).getExpression());
		
		//remove single terms from multi-term expressions
		
		controller.removeTerm(orId, t3);
		
		assertEquals(1, state.numQueryGroups());
		assertEquals(new Or(t2, t4), state.getQuery(orId).getExpression());
		
		controller.removeTerm(orId, t4);
		
		assertEquals(1, state.numQueryGroups());
		assertEquals(t2, state.getQuery(orId).getExpression());
		
		controller.removeTerm(orId, t2);
		
		assertEquals(0, state.numQueryGroups());
	}
}
