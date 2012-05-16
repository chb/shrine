package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.AbstractWebclientTest;
import net.shrine.webclient.client.Events;
import net.shrine.webclient.client.QueryBuildingController;
import net.shrine.webclient.client.State;
import net.shrine.webclient.client.domain.Term;

import org.junit.Test;

/**
 * 
 * @author clint
 * @date Apr 24, 2012
 */
public class QueryTermTestGwt extends AbstractWebclientTest {

	@Test
	public void testQueryTerm() {
		try {
			new QueryTerm(42, null, term("foo"));
			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }
		
		try {
			new QueryTerm(42, new QueryBuildingController(state()), null);
			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }
		
		try {
			new QueryTerm(42, null, null);
			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }
		
		final String simpleName = "simple name";
		final String path = "/fully/qualified/path";
		
		final QueryTerm qt = new QueryTerm(99, new QueryBuildingController(state()), new Term(path, "some-bogus-category", simpleName));
		
		assertEquals(simpleName, qt.termSpan.getInnerText());
		assertEquals(path, qt.getTitle());
	}
	
	@Test
	public void testClickCloseButton() {
		final State state = state();
		
		final QueryBuildingController controller = new QueryBuildingController(state);
		
		final String simpleName = "simple name";
		final String path = "/fully/qualified/path";
		final Term term = new Term(path, "some-bogus-category", simpleName);
		
		final int id1 = state.registerNewQuery(term("nuh")).getId();
		
		final int id2 = state.registerNewQuery(term).getId();
		
		assertEquals(2, state.numQueryGroups());
		
		state.getQuery(id1);
		state.getQuery(id2);
		
		final QueryTerm qt = new QueryTerm(id2, controller, term);
		
		qt.closeButton.fireEvent(Events.click());
		
		assertEquals(1, state.numQueryGroups());
		
		state.getQuery(id1);
		
		try {
			state.getQuery(id2);
			fail("should have thrown");
		} catch(IllegalArgumentException expected) { }
	}
}
