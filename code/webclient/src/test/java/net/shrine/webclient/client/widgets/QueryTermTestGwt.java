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
			new QueryTerm(null, new QueryBuildingController(new State()), new Term("foo"));
			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }
		
		try {
			new QueryTerm(id("asdasdasasdgfa"), null, new Term("foo"));
			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }
		
		try {
			new QueryTerm(id("asdjksdlksl"), new QueryBuildingController(new State()), null);
			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }
		
		try {
			new QueryTerm(null, null, null);
			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }
		
		final String simpleName = "simple name";
		final String path = "/fully/qualified/path";
		
		final QueryTerm qt = new QueryTerm(id("asdjksdlksl"), new QueryBuildingController(new State()), new Term(path, simpleName));
		
		assertEquals(simpleName, qt.termSpan.getInnerText());
		assertEquals(path, qt.getTitle());
	}
	
	@Test
	public void testClickCloseButton() {
		final State state = new State();
		
		final QueryBuildingController controller = new QueryBuildingController(state);
		
		final String simpleName = "simple name";
		final String path = "/fully/qualified/path";
		final Term term = new Term(path, simpleName);
		
		state.registerNewQuery(new Term("nuh"));
		state.registerNewQuery(term);
		
		assertEquals(2, state.numQueryGroups());
		final String queryName = "B";
		state.getQuery(id(queryName));
		
		final QueryTerm qt = new QueryTerm(id(queryName), controller, term);
		
		qt.closeButton.fireEvent(Events.click());
		
		assertEquals(1, state.numQueryGroups());
		
		try {
			state.getQuery(id(queryName));
			fail("should have thrown");
		} catch(IllegalArgumentException expected) { }
	}
}
