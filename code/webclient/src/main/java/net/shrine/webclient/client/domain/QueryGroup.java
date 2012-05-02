package net.shrine.webclient.client.domain;

import java.util.Date;

import net.shrine.webclient.client.QueryGroupId;
import net.shrine.webclient.client.util.AbstractObservable;
import net.shrine.webclient.client.util.Formats;
import net.shrine.webclient.client.util.Util;

/**
 * 
 * @author clint
 * @date Mar 26, 2012
 * 
 */
public final class QueryGroup extends AbstractObservable implements XmlAble, ReadOnlyQueryGroup, Comparable<QueryGroup> {
	private Expression expression;

	private boolean negated = false;

	private Date start = null;

	private Date end = null;

	private int minOccurances = 1;

	private QueryGroupId id;

	private final Date createdOn = new Date();

	public QueryGroup(final QueryGroupId id, final Expression expression) {
		super();

		Util.requireNotNull(expression);
		Util.requireNotNull(id);

		this.expression = expression;
		this.id = id;
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
	public QueryGroupId getId() {
		return id;
	}

	public void setId(final QueryGroupId id) {
		Util.requireNotNull(id);

		try {
			this.id = id;
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
		final int prime = 31;
		int result = 1;
		result = prime * result + (id == null ? 0 : id.hashCode());
		return result;
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
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}
}
