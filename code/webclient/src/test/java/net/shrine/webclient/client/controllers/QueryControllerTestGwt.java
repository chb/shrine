package net.shrine.webclient.client.controllers;

import net.shrine.webclient.client.AbstractWebclientTest;
import net.shrine.webclient.client.state.QueryCompletedEvent;
import net.shrine.webclient.client.state.QueryCompletedEventHandler;
import net.shrine.webclient.client.state.QueryStartedEvent;
import net.shrine.webclient.client.state.QueryStartedEventHandler;
import net.shrine.webclient.client.state.State;
import net.shrine.webclient.shared.domain.And;
import net.shrine.webclient.shared.domain.MultiInstitutionQueryResult;
import net.shrine.webclient.shared.domain.Term;

import org.fusesource.restygwt.client.MethodCallback;
import org.junit.Test;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;

/**
 * 
 * @author clint
 * @date Apr 20, 2012
 */
public class QueryControllerTestGwt extends AbstractWebclientTest {

    private State state;

    private QueryController controller;

    private QueryBuildingController queryBuildingController;

    private MockQueryService queryService;

    private EventBus eventBus;

    private SimpleQueryStartedEventHandler queryStartedListener;

    private SimpleQueryCompletedEventHandler queryCompletedListener;

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();

        eventBus = new SimpleEventBus();

        queryStartedListener = new SimpleQueryStartedEventHandler();
        queryCompletedListener = new SimpleQueryCompletedEventHandler();

        eventBus.addHandler(QueryStartedEvent.getType(), queryStartedListener);
        eventBus.addHandler(QueryCompletedEvent.getType(), queryCompletedListener);

        state = state();

        queryService = new MockQueryService();
        controller = new QueryController(state, queryService, eventBus);
        queryBuildingController = new QueryBuildingController(state);
    }

    @Override
    protected void gwtTearDown() throws Exception {
        super.gwtTearDown();

        eventBus = null;
        queryStartedListener = null;
        queryCompletedListener = null;
        state = null;
        queryService = null;
        controller = null;
        queryBuildingController = null;
    }

    @Test
    public void testRunQuery() {
        assertFalse(queryStartedListener.triggered);
        assertFalse(queryCompletedListener.triggered);

        try {
            controller.runQuery();

            fail("Should have thrown when no queries to run");
        } catch (final IllegalArgumentException expected) {}

        resetListeners();

        final Term t1 = term("nuh");

        final int t1Id = queryBuildingController.addNewTerm(t1).getId();

        controller.runQuery();

        assertTrue(queryStartedListener.triggered);
        assertTrue(queryCompletedListener.triggered);

        assertEquals(t1.toXmlString(), state.getQueryExpression());
        assertEquals(t1.toXmlString(), state.getQuery(t1Id).getExpression().toXmlString());

        assertEquals(queryService.multiNodeResultsToReturn.asMap(), state.getQueryResult().get());

        final Term t2 = term("zuh");

        final int t2Id = queryBuildingController.addNewTerm(t2).getId();

        state.getQueryResult().clear();

        resetListeners();

        controller.runQuery();

        assertTrue(queryStartedListener.triggered);
        assertTrue(queryCompletedListener.triggered);

        assertEquals(new And(t1, t2).toXmlString(), state.getQueryExpression());
        assertEquals(t1.toXmlString(), state.getQuery(t1Id).getExpression().toXmlString());
        assertEquals(t2.toXmlString(), state.getQuery(t2Id).getExpression().toXmlString());

        assertEquals(queryService.multiNodeResultsToReturn.asMap(), state.getQueryResult().get());
    }

    @Test
    public void testRunQueryQueryServiceThrows() {
        queryService = new AlwaysThrowsQueryService();
        controller = new QueryController(state, queryService, eventBus);
        
        assertFalse(queryStartedListener.triggered);
        assertFalse(queryCompletedListener.triggered);
        
        final Term t1 = term("nuh");

        queryBuildingController.addNewTerm(t1).getId();

        controller.runQuery();
        
        assertTrue(queryStartedListener.triggered);
        assertTrue(queryCompletedListener.triggered);
    }
    
    @Test
    public void testRunQueryQueryServiceRemoteCallFails() {
        queryService = new AlwaysFailsQueryService();
        controller = new QueryController(state, queryService, eventBus);
        
        assertFalse(queryStartedListener.triggered);
        assertFalse(queryCompletedListener.triggered);
        
        final Term t1 = term("nuh");

        queryBuildingController.addNewTerm(t1).getId();

        controller.runQuery();
        
        assertTrue(queryStartedListener.triggered);
        assertTrue(queryCompletedListener.triggered);
    }
    
    private void resetListeners() {
        queryStartedListener.triggered = false;
        queryCompletedListener.triggered = false;
    }

    private static final class AlwaysThrowsQueryService extends MockQueryService {
        @Override
        public void performQuery(final String expr, final MethodCallback<MultiInstitutionQueryResult> callback) {
            throw new RuntimeException("blarg");
        }
    }
    
    private static final class AlwaysFailsQueryService extends MockQueryService {
        @Override
        public void performQuery(final String expr, final MethodCallback<MultiInstitutionQueryResult> callback) {
            callback.onFailure(null, new RuntimeException("foo"));
        }
    }
    
    private static final class SimpleQueryStartedEventHandler implements QueryStartedEventHandler {

        public boolean triggered = false;

        @Override
        public void handle(final QueryStartedEvent event) {
            triggered = true;
        }
    }

    private static final class SimpleQueryCompletedEventHandler implements QueryCompletedEventHandler {

        public boolean triggered = false;

        @Override
        public void handle(final QueryCompletedEvent event) {
            triggered = true;
        }
    }
}
