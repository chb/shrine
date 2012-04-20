package net.shrine.webclient.client;

import java.util.HashMap;

import net.shrine.webclient.client.domain.And;
import net.shrine.webclient.client.domain.IntWrapper;
import net.shrine.webclient.client.domain.Term;

import org.junit.Test;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author clint
 * @date Apr 20, 2012
 */
public class QueryControllerTestGwt extends AbstractWebclientTest {
	
	private State state;
	
	private QueryController controller;
	
	private QueryBuildingController queryBuildingController;
	
	private MockQueryServiceAsync queryService;
	
	@Override
	protected void gwtSetUp() throws Exception {
		super.gwtSetUp();
		
		state = new State();
		queryService = new MockQueryServiceAsync();
		controller = new QueryController(state, queryService);
		queryBuildingController = new QueryBuildingController(state);
	}

	@Override
	protected void gwtTearDown() throws Exception {
		super.gwtTearDown();
		
		state = null;
		queryService = null;
		controller = null;
		queryBuildingController = null;
	}

	@Test
	public void testRunEveryQuery() {
		try {
			controller.runEveryQuery();
			
			fail("Should have thrown when no queries to run");
		} catch(IllegalArgumentException expected) { }
		
		final Term t1 = new Term("nuh");
		
		queryBuildingController.addNewTerm(t1);
		
		controller.runEveryQuery();
		
		assertEquals(t1.toXmlString(), state.getAllExpression());
		assertEquals(t1.toXmlString(), state.getQueries().get("A").getExpression().toXmlString());
		
		assertEquals(queryService.multiNodeResultsToReturn, state.getAllResult().get());
		assertEquals(queryService.multiNodeResultsToReturn, state.getQueries().get("A").getResult().get());
		
		final Term t2 = new Term("zuh");
		
		queryBuildingController.addNewTerm(t2);
		
		state.getAllResult().clear();
		state.getQueries().get("A").getResult().clear();
		
		controller.runEveryQuery();
		
		assertEquals(new And(t1, t2).toXmlString(), state.getAllExpression());
		assertEquals(t1.toXmlString(), state.getQueries().get("A").getExpression().toXmlString());
		assertEquals(t2.toXmlString(), state.getQueries().get("B").getExpression().toXmlString());
		
		assertEquals(queryService.multiNodeResultsToReturn, state.getAllResult().get());
		assertEquals(queryService.multiNodeResultsToReturn, state.getQueries().get("A").getResult().get());
		assertEquals(queryService.multiNodeResultsToReturn, state.getQueries().get("B").getResult().get());
	}

	@Test
	public void testRunQuery() {
		try {
			controller.runQuery("foo");
		
			fail("Should have thrown on unknown query");
		} catch(IllegalArgumentException expected) { }
		
		final Term t1 = new Term("nuh");
		
		queryBuildingController.addNewTerm(t1);
		
		controller.runQuery("A");
		
		assertEquals(t1.toXmlString(), state.getAllExpression());
		assertEquals(t1.toXmlString(), state.getQueries().get("A").getExpression().toXmlString());
		
		assertTrue(state.getAllResult().isEmpty());
		assertEquals(queryService.multiNodeResultsToReturn, state.getQueries().get("A").getResult().get());
		
		final Term t2 = new Term("zuh");
		
		state.getAllResult().clear();
		state.getQueries().get("A").getResult().clear();
		
		queryBuildingController.addNewTerm(t2);
		
		controller.runQuery("B");
		
		assertEquals(new And(t1, t2).toXmlString(), state.getAllExpression());
		assertEquals(t2.toXmlString(), state.getQueries().get("B").getExpression().toXmlString());

		assertTrue(state.getQueries().get("A").getResult().isEmpty());
		assertTrue(state.getAllResult().isEmpty());
		assertEquals(queryService.multiNodeResultsToReturn, state.getQueries().get("B").getResult().get());
	}

	private static final Integer total = 99;
	
	@SuppressWarnings("serial")
	private static final HashMap<String, IntWrapper> multiNodeResults = new HashMap<String, IntWrapper>() {{
		this.put("foo", new IntWrapper(99));
		this.put("bar", new IntWrapper(42));
	}};
	
	private static final class MockQueryServiceAsync implements QueryServiceAsync {
		public Integer totalToReturn = total;
		
		public HashMap<String, IntWrapper> multiNodeResultsToReturn = multiNodeResults;
		
		@Override
		public void query(final String expr, final AsyncCallback<Integer> callback) {
			callback.onSuccess(totalToReturn);
		}

		@Override
		public void queryForBreakdown(final String expr, final AsyncCallback<HashMap<String, IntWrapper>> callback) {
			callback.onSuccess(multiNodeResultsToReturn);
		}
	}
}
