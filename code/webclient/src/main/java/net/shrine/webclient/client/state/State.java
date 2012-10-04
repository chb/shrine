package net.shrine.webclient.client.state;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.shrine.webclient.client.util.Observable;
import net.shrine.webclient.client.util.ObservableList;
import net.shrine.webclient.client.util.Observer;
import net.shrine.webclient.client.util.QueryNameIterator;
import net.shrine.webclient.client.util.SimpleObserver;
import net.shrine.webclient.client.util.Util;
import net.shrine.webclient.shared.domain.Expression;
import net.shrine.webclient.shared.domain.SingleInstitutionQueryResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.EventBus;

/**
 * 
 * @author clint
 * @date Mar 30, 2012
 */
public final class State {

    private final EventBus eventBus;

    private String queryExpressionXml = null;

    private final Observable<Map<String, SingleInstitutionQueryResult>> queryResult = Observable.empty();

    // Query group name => QueryGroup (Expression, integer result (patient set
    // size), negated (t/f), start date, end date, min occurrances )
    private final ObservableList<QueryGroup> queryGroups = ObservableList.empty();

    // React to changes in query list by renaming queries (to preserve A ... Z
    // naming)
    @SuppressWarnings("unused")
    private final Observer queryRenamer = new SimpleObserver(queryGroups) {
        @Override
        public void inform() {
            reNameQueries();
        }
    };

    // React to changes in query list by firing events
    @SuppressWarnings("unused")
    private final Observer queryGroupListChangeEventForwarder = new SimpleObserver(queryGroups) {
        @Override
        public void inform() {
            fireQueryGroupsChangedEvent();
        }
    };

    public State(final EventBus eventBus) {
        super();

        Util.requireNotNull(eventBus);

        this.eventBus = eventBus;

        this.eventBus.addHandler(SingleQueryGroupChangedEvent.getType(), new SingleQueryGroupChangedEventHandler() {
            @Override
            public void handle(final SingleQueryGroupChangedEvent event) {
                fireQueryGroupsChangedEvent();
            }
        });
    }

    public void guardQueryIsPresent(final int id) {
        Util.require(isQueryIdPresent(id));
    }

    public void guardQueryIsNotPresent(final int id) {
        Util.require(!isQueryIdPresent(id));
    }

    public boolean isQueryIdPresent(final int id) {
        for (final QueryGroup group : queryGroups) {
            if (id == group.getId()) {
                return true;
            }
        }

        return false;
    }

    // Make sure queries are named A,B,C,... Z, in that order, with no gaps,
    // always starting from 'A'
    private void reNameQueries() {
        final Iterator<String> newIdIter = new QueryNameIterator();

        for (final QueryGroup group : queryGroups) {
            group.setName(newIdIter.next());
        }
    }

    public void removeQuery(final int id) {
        guardQueryIsPresent(id);

        final QueryGroup query = getQuery(id);

        queryGroups.remove(query);
    }

    public int numQueryGroups() {
        return queryGroups.size();
    }

    public void completeQuery(final Map<String, SingleInstitutionQueryResult> resultsByInstitution) {
        if (Log.isInfoEnabled()) {
            Log.info("Completing query with: '" + resultsByInstitution + "'");

            for (final Entry<String, SingleInstitutionQueryResult> entry : resultsByInstitution.entrySet()) {
                Log.info(entry.getKey() + ": " + entry.getValue());
            }
        }

        queryResult.set(resultsByInstitution);
    }

    public QueryGroup getQuery(final int id) {
        for (final QueryGroup query : queryGroups) {
            if (id == query.getId()) {
                return query;
            }
        }

        throw new IllegalArgumentException("No query with id '" + id + "' exists");
    }

    private QueryGroup addNewQuery(final Expression expr) {
        final QueryGroup newQuery = new QueryGroup(eventBus, "NULL", expr);

        queryGroups.add(newQuery);

        Log.info("Added query group '" + newQuery.getName() + "' (" + newQuery.getId() + "): " + newQuery.getExpression());

        return newQuery;
    }

    public QueryGroup registerNewQuery(final Expression expr) {

        final QueryGroup newQuery = addNewQuery(expr);

        updateQueryExpression();

        return newQuery;
    }

    public void updateQueryExpression() {
        queryExpressionXml = ExpressionXml.fromQueryGroups(queryGroups);
    }

    public ObservableList<QueryGroup> getQueryGroups() {
        return queryGroups;
    }

    public String getQueryExpression() {
        return queryExpressionXml;
    }

    public Observable<Map<String, SingleInstitutionQueryResult>> getQueryResult() {
        return queryResult;
    }

    void fireQueryGroupsChangedEvent() {
        State.this.eventBus.fireEvent(new QueryGroupsChangedEvent(queryGroups));
    }
}
