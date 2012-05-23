package net.shrine.webclient.client;

import java.util.List;

import net.shrine.webclient.client.domain.Or;
import net.shrine.webclient.client.domain.QueryGroup;
import net.shrine.webclient.client.domain.Term;
import net.shrine.webclient.client.util.MockObserver;
import net.shrine.webclient.client.util.Util;

import org.junit.Test;

/**
 * 
 * @author clint
 * @date Apr 19, 2012
 */
public class QueryBuildingControllerTestGwt extends AbstractWebclientTest {

	@Test
	public void testMoveTerm() {
		// moving to same group shouldn't delete term
		{
			final State state = state();

			final QueryBuildingController controller = new QueryBuildingController(state);

			final Term t1 = term("foo");
			final Term t2 = term("bar");

			final int id1 = state.registerNewQuery(t1).getId();
			
			assertEquals(t1, state.getQuery(id1).getExpression());
			assertEquals(1, state.numQueryGroups());
			
			controller.moveTerm(t1, id1, id1);
			
			assertEquals(t1, state.getQuery(id1).getExpression());
			assertEquals(1, state.numQueryGroups());
			
			controller.removeAllQueryGroups();
			
			assertEquals(0, state.numQueryGroups());
			
			final Or or = new Or(t1, t2);
			
			final int id2 = state.registerNewQuery(or).getId();
			
			assertEquals(or, state.getQuery(id2).getExpression());
			assertEquals(1, state.numQueryGroups());
			
			controller.moveTerm(t1, id2, id2);
			
			assertEquals(or, state.getQuery(id2).getExpression());
			assertEquals(1, state.numQueryGroups());
			
			controller.moveTerm(t2, id2, id2);
			
			assertEquals(or, state.getQuery(id2).getExpression());
			assertEquals(1, state.numQueryGroups());
		}
		
		// to new query, from query only contains 1 term
		{
			final State state = state();

			final QueryBuildingController controller = new QueryBuildingController(state);

			final Term t1 = term("foo");

			final int id = state.registerNewQuery(t1).getId();

			final MockObserver observer = new MockObserver(state.getQueries());

			assertEquals(1, state.numQueryGroups());
			assertEquals(t1, state.getQuery(id).getExpression());
			assertFalse(observer.informed);

			controller.moveTerm(t1, id, QueryGroup.NullId);

			assertEquals(1, state.numQueryGroups());
			assertEquals(t1, state.getQuery(id).getExpression());
			assertTrue(observer.informed);
		}

		// to new query, from query contains > 1 term
		{
			final State state = state();

			final QueryBuildingController controller = new QueryBuildingController(state);

			final Term t1 = term("foo");
			final Term t2 = term("bar");
			final Term t3 = term("nuh");

			final Or or = new Or(t1, t2, t3);

			final int id = state.registerNewQuery(or).getId();

			final MockObserver observer = new MockObserver(state.getQueries());

			assertEquals(1, state.numQueryGroups());
			assertEquals(or, state.getQuery(id).getExpression());
			assertFalse(observer.informed);

			controller.moveTerm(t1, id, QueryGroup.NullId);

			assertTrue(observer.informed);

			assertEquals(2, state.numQueryGroups());

			final List<QueryGroup> queries = Util.sorted(state.getQueries());

			assertEquals(or.without(t1), queries.get(0).getExpression());
			assertEquals(t1, queries.get(1).getExpression());
		}

		// to existing query
		{
			final State state = state();

			final QueryBuildingController controller = new QueryBuildingController(state);

			final Term t1 = term("foo");
			final Term t2 = term("bar");
			final Term t3 = term("nuh");
			final Term t4 = term("zuh");

			final Or or1 = new Or(t1, t2);
			final Or or2 = new Or(t3, t4);

			final int id1 = state.registerNewQuery(or1).getId();
			final int id2 = state.registerNewQuery(or2).getId();

			final MockObserver observer = new MockObserver(state.getQueries());

			assertEquals(2, state.numQueryGroups());
			assertEquals(or1, state.getQuery(id1).getExpression());
			assertEquals(or2, state.getQuery(id2).getExpression());
			assertFalse(observer.informed);

			controller.moveTerm(t1, id1, id2);

			assertFalse(observer.informed);

			assertEquals(2, state.numQueryGroups());

			{
				final List<QueryGroup> queries = Util.sorted(state.getQueries());

				assertEquals(t2, queries.get(0).getExpression());
				assertEquals(or2.with(t1), queries.get(1).getExpression());
			}

			controller.moveTerm(t2, id1, id2);

			assertTrue(observer.informed);

			assertEquals(1, state.numQueryGroups());

			assertEquals(or2.with(t1).with(t2), state.getQuery(id2).getExpression());
		}

		// to existing 1-term query
		{
			final State state = state();

			final QueryBuildingController controller = new QueryBuildingController(state);

			final Term t1 = term("foo");
			final Term t2 = term("bar");
			final Term t3 = term("nuh");

			final Or or = new Or(t1, t2);

			final int id1 = state.registerNewQuery(or).getId();
			final int id2 = state.registerNewQuery(t3).getId();

			final MockObserver observer = new MockObserver(state.getQueries());

			assertEquals(2, state.numQueryGroups());
			assertEquals(or, state.getQuery(id1).getExpression());
			assertEquals(t3, state.getQuery(id2).getExpression());
			assertFalse(observer.informed);

			controller.moveTerm(t1, id1, id2);

			assertFalse(observer.informed);

			assertEquals(2, state.numQueryGroups());

			final List<QueryGroup> queries = Util.sorted(state.getQueries());

			assertEquals(t2, queries.get(0).getExpression());
			assertEquals(new Or(t3, t1), queries.get(1).getExpression());
		}
	}

	@Test
	public void testAddNewTerm() {
		final State state = state();

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
		final State state = state();

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
		final State state = state();

		final QueryBuildingController controller = new QueryBuildingController(state);

		final int fooId = state.registerNewQuery(term("foo")).getId();
		final int barId = state.registerNewQuery(term("blah")).getId();

		assertEquals(2, state.numQueryGroups());

		try {
			controller.removeQueryGroup(9483);

			fail("should have thrown");
		} catch (IllegalArgumentException expected) { }

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
		final State state = state();

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
		} catch (IllegalArgumentException expected) {
		}

		// Try to remove term that's not part of this query group - shouldn't do
		// anything
		controller.removeTerm(t1Id, t2);

		assertEquals(2, state.numQueryGroups());

		assertEquals(t1, state.getQuery(t1Id).getExpression());
		assertEquals(or, state.getQuery(orId).getExpression());

		// Remove term that's actually present

		// Single-term expressions should be removed
		controller.removeTerm(t1Id, t1);

		assertEquals(1, state.numQueryGroups());
		assertEquals(or, state.getQuery(orId).getExpression());

		// remove single terms from multi-term expressions

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
