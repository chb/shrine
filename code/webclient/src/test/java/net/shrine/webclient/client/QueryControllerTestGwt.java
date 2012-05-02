package net.shrine.webclient.client;

import net.shrine.webclient.client.domain.And;
import net.shrine.webclient.client.domain.Term;

import org.junit.Test;


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
		assertEquals(t1.toXmlString(), state.getQuery(id("A")).getExpression().toXmlString());
		
		assertEquals(queryService.multiNodeResultsToReturn, state.getAllResult().get());
		
		final Term t2 = new Term("zuh");
		
		queryBuildingController.addNewTerm(t2);
		
		state.getAllResult().clear();
		
		controller.runEveryQuery();
		
		assertEquals(new And(t1, t2).toXmlString(), state.getAllExpression());
		assertEquals(t1.toXmlString(), state.getQuery(id("A")).getExpression().toXmlString());
		assertEquals(t2.toXmlString(), state.getQuery(id("B")).getExpression().toXmlString());
		
		assertEquals(queryService.multiNodeResultsToReturn, state.getAllResult().get());
	}
}
