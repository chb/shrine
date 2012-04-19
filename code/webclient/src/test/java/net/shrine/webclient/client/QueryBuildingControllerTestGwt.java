package net.shrine.webclient.client;

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
		
		final QueryGroup group = state.getQueries().get("A");
		
		assertEquals(term1, group.getExpression());
		
		controller.addNewTerm(term2);
		
		assertEquals(2, state.numQueryGroups());
		
		assertEquals(term1, state.getQueries().get("A").getExpression());
		assertEquals(term2, state.getQueries().get("B").getExpression());
	}
}
