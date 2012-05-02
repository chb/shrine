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
	public void testAddNewTerm() {
		final State state = new State();
		
		final QueryBuildingController controller = new QueryBuildingController(state);
		
		final Term term1 = new Term("foo");
		final Term term2 = new Term("bar");
		
		assertEquals(0, state.numQueryGroups());
		
		controller.addNewTerm(term1);
		
		assertEquals(1, state.numQueryGroups());
		
		final QueryGroup group = state.getQuery(id("A"));
		
		assertEquals(term1, group.getExpression());
		
		controller.addNewTerm(term2);
		
		assertEquals(2, state.numQueryGroups());
		
		assertEquals(term1, state.getQuery(id("A")).getExpression());
		assertEquals(term2, state.getQuery(id("B")).getExpression());
	}
	
	public void testRemoveAllQueryGroups() {
		final State state = new State();
		
		final QueryBuildingController controller = new QueryBuildingController(state);
		
		assertEquals(0, state.numQueryGroups());
		
		controller.removeAllQueryGroups();
		
		assertEquals(0, state.numQueryGroups());
		
		state.registerNewQuery(id("foo"), new Term("foo"));
		state.registerNewQuery(id("bar"), new Term("blah"));
		
		assertEquals(2, state.numQueryGroups());
		
		controller.removeAllQueryGroups();
		
		assertEquals(0, state.numQueryGroups());
	}
	
	public void testRemoveQueryGroup() {
		final State state = new State();
		
		final QueryBuildingController controller = new QueryBuildingController(state);
		
		state.registerNewQuery(id("foo"), new Term("foo"));
		state.registerNewQuery(id("bar"), new Term("blah"));
		
		assertEquals(2, state.numQueryGroups());
		
		try {
			controller.removeQueryGroup(id("aksljdklsadj"));
			
			fail("should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		controller.removeQueryGroup(id("foo"));
		
		assertEquals(1, state.numQueryGroups());
		
		state.guardQueryIsPresent(id("bar"));
		
		controller.removeQueryGroup(id("bar"));
		
		assertEquals(0, state.numQueryGroups());
	}
	
	public void testRemoveTerm() {
		final State state = new State();
		
		final QueryBuildingController controller = new QueryBuildingController(state);
		
		final Term t1 = new Term("foo");
		final Term t2 = new Term("blah");
		final Term t3 = new Term("nuh");
		final Term t4 = new Term("zuh");
		final Or or = new Or(t2, t3, t4);
		
		state.registerNewQuery(id("foo"), t1);
		
		state.registerNewQuery(id("bar"), or);
		
		assertEquals(2, state.numQueryGroups());
		
		try {
			controller.removeTerm(id("aksljdklsadj"), t1);
			
			fail("should have thrown on unknown query group");
		} catch(IllegalArgumentException expected) { }
		
		//Try to remove term that's not part of this query group - shouldn't do anything
		controller.removeTerm(id("foo"), t2);
		
		assertEquals(2, state.numQueryGroups());
		
		assertEquals(t1, state.getQuery(id("foo")).getExpression());
		assertEquals(or, state.getQuery(id("bar")).getExpression());
		
		//Remove term that's actually present
		
		//Single-term expressions should be removed
		controller.removeTerm(id("foo"), t1);
		
		assertEquals(1, state.numQueryGroups());
		assertFalse(state.isQueryIdPresent(id("foo")));
		assertEquals(or, state.getQuery(id("bar")).getExpression());
		
		//remove single terms from multi-term expressions
		
		controller.removeTerm(id("bar"), t3);
		
		assertEquals(1, state.numQueryGroups());
		assertEquals(new Or(t2, t4), state.getQuery(id("bar")).getExpression());
		
		controller.removeTerm(id("bar"), t4);
		
		assertEquals(1, state.numQueryGroups());
		assertEquals(new Or(t2), state.getQuery(id("bar")).getExpression());
		
		controller.removeTerm(id("bar"), t2);
		
		assertEquals(0, state.numQueryGroups());
	}
}
