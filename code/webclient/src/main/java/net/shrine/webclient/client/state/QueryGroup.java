package net.shrine.webclient.client.state;

import java.util.Date;

import net.shrine.webclient.client.util.EventCreator;
import net.shrine.webclient.client.util.FiresEventsObservable;
import net.shrine.webclient.client.util.Formats;
import net.shrine.webclient.client.util.Util;
import net.shrine.webclient.shared.domain.Expression;
import net.shrine.webclient.shared.domain.Or;
import net.shrine.webclient.shared.domain.Term;
import net.shrine.webclient.shared.domain.XmlAble;

import com.google.gwt.event.shared.EventBus;

/**
 * 
 * @author clint
 * @date Mar 26, 2012
 * 
 */
public final class QueryGroup extends FiresEventsObservable<SingleQueryGroupChangedEvent> implements XmlAble, ReadOnlyQueryGroup, Comparable<QueryGroup> {
    private Expression expression;

    private boolean negated = false;

    private Date start = null;

    private Date end = null;

    private int minOccurances = 1;

    private final Date createdOn = new Date();

    private String name;

    private final int id;

    private static int nextId = 1;

    public static final int NullId = 0;

    static int getNextId() {
        return nextId;
    }

    public QueryGroup(final EventBus eventBus, final String name, final Expression expression) {
        this(eventBus, nextId++, name, expression);
    }

    public QueryGroup(final EventBus eventBus, final int id, final String name, final Expression expression) {
        super(eventBus);

        this.wireUp(new EventCreator<SingleQueryGroupChangedEvent>() {
            @Override
            public SingleQueryGroupChangedEvent create() {
                return new SingleQueryGroupChangedEvent(QueryGroup.this);
            }
        });

        Util.requireNotNull(expression);
        Util.requireNotNull(name);

        this.expression = expression;
        this.id = id;
        this.name = name;
    }

    @Override
    public int compareTo(final QueryGroup other) {
        return createdOn.compareTo(other.createdOn);
    }

    static String dateToXml(final String tagName, final Date date) {
        return date == null ? "" : "<" + tagName + ">" + Formats.Date.iso8601.format(date) + "</" + tagName + ">";
    }

    @Override
    public String toXmlString() {
        final String exprString = expression.toXmlString();

        String result;

        if (negated) {
            result = "<not>" + exprString + "</not>";
        } else {
            result = exprString;
        }

        if (start != null || end != null) {
            result = "<dateBounded>" + dateToXml("start", start) + dateToXml("end", end) + result + "</dateBounded>";
        }

        if (minOccurances != 1) {
            result = "<occurs><min>" + minOccurances + "</min>" + result + "</occurs>";
        }

        return result;
    }

    @Override
    public Expression getExpression() {
        return expression;
    }

    public void setExpression(final Expression expression) {
        Util.requireNotNull(expression);
        Util.require(expression instanceof Term || expression instanceof Or, "Only terms or disjunctions can be represented as a QueryGroup");

        try {
            this.expression = expression;
        } finally {
            notifyObservers();
        }
    }

    @Override
    public int getMinOccurances() {
        return minOccurances;
    }

    public void setMinOccurances(final int minOccurances) {
        Util.require(minOccurances > 0);

        try {
            this.minOccurances = minOccurances;
        } finally {
            notifyObservers();
        }
    }

    @Override
    public boolean isNegated() {
        return negated;
    }

    public void setNegated(final boolean negated) {
        try {
            this.negated = negated;
        } finally {
            notifyObservers();
        }
    }

    @Override
    public Date getStart() {
        return start;
    }

    public void setStart(final Date newStart) {
        try {
            start = newStart;
        } finally {
            notifyObservers();
        }
    }

    @Override
    public Date getEnd() {
        return end;
    }

    public void setEnd(final Date newEnd) {
        try {
            end = newEnd;
        } finally {
            notifyObservers();
        }
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        Util.requireNotNull(name);

        try {
            this.name = name;
        } finally {
            notifyObservers();
        }
    }

    @Override
    public Date getCreatedOn() {
        return createdOn;
    }

    @Override
    public String toString() {
        return "QueryGroup [id=" + id + "]";
    }

    @Override
    public int hashCode() {
        return 31 + id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QueryGroup other = (QueryGroup) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }
}
