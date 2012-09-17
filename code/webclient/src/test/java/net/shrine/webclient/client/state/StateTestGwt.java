package net.shrine.webclient.client.state;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.shrine.webclient.client.AbstractWebclientTest;
import net.shrine.webclient.shared.domain.And;
import net.shrine.webclient.shared.domain.Breakdown;
import net.shrine.webclient.shared.domain.SingleInstitutionQueryResult;
import net.shrine.webclient.shared.domain.Term;

import org.junit.Test;

/**
 * 
 * @author clint
 * @date Apr 19, 2012
 */
public class StateTestGwt extends AbstractWebclientTest {

    @Test
    public void testGuardQueryNameIsPresent() {
        final State state = state();

        try {
            state.guardQueryIsPresent(12344);
            fail("Should have thrown");
        } catch (IllegalArgumentException expected) {
        }

        final int id = state.registerNewQuery(term("foo")).getId();

        state.guardQueryIsPresent(id);
    }

    @Test
    public void testNumQueryGroups() {
        final State state = state();

        assertEquals(0, state.numQueryGroups());

        state.registerNewQuery(term("foo"));

        assertEquals(1, state.numQueryGroups());

        state.registerNewQuery(term("foo"));

        assertEquals(2, state.numQueryGroups());
    }

    @Test
    public void testCompleteAllQuery() {
        final State state = state();

        @SuppressWarnings("serial")
        final Map<String, SingleInstitutionQueryResult> results = new HashMap<String, SingleInstitutionQueryResult>() {{
            this.put("foo", new SingleInstitutionQueryResult(5L, Collections.<String, Breakdown>emptyMap()));
            this.put("bar", new SingleInstitutionQueryResult(99L, Collections.<String, Breakdown>emptyMap()));
        }};

        assertTrue(state.getQueryResult().isEmpty());

        state.completeQuery(results);

        assertTrue(state.getQueryResult().isDefined());

        assertEquals(results, state.getQueryResult().get());
    }

    @Test
    public void testRegisterNewQuery() {
        final State state = state();

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
        final State state = state();

        final Term t1 = term("foo");
        final Term t2 = term("bar");

        try {
            state.updateQueryExpression();

            fail("Should have thrown with no query groups");
        } catch (IllegalArgumentException expected) {
        }

        state.registerNewQuery(t1);

        state.updateQueryExpression();

        assertEquals(t1.toXmlString(), state.getQueryExpression());

        state.registerNewQuery(t2);

        state.updateQueryExpression();

        assertEquals(new And(t1, t2).toXmlString(), state.getQueryExpression());
    }
}
