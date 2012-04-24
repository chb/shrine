package net.shrine.webclient.client.domain;

import java.util.Date;
import java.util.HashMap;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

import net.shrine.webclient.client.util.AbstractObservable;
import net.shrine.webclient.client.util.Observable;
import net.shrine.webclient.client.util.Util;

/**
 * 
 * @author clint
 * @date Mar 26, 2012
 * 
 */
public final class QueryGroup extends AbstractObservable implements XmlAble, ReadOnlyQueryGroup {
	private Expression expression;

	private final Observable<HashMap<String, IntWrapper>> result;

	private boolean negated = false;

	private Date start = null;

	private Date end = null;

	private int minOccurances = 1;

	public QueryGroup(final Expression expression, final Observable<HashMap<String, IntWrapper>> result) {
		super();
		
		Util.requireNotNull(expression);
		Util.requireNotNull(result);

		this.expression = expression;
		this.result = result;
	}

	private static final DateTimeFormat dateFormat = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
	
	static String dateToXml(final String tagName, final Date date) {
		return date == null ? "" : ("<" + tagName + ">" + dateFormat.format(date) + "</" + tagName + ">");
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
	public Observable<HashMap<String, IntWrapper>> getResult() {
		return result;
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
			this.start = newStart;
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
			this.end = newEnd;
		} finally {
			notifyObservers();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (end == null ? 0 : end.hashCode());
		result = prime * result + (expression == null ? 0 : expression.hashCode());
		result = prime * result + (negated ? 1231 : 1237);
		result = prime * result + (this.result == null ? 0 : this.result.hashCode());
		result = prime * result + (start == null ? 0 : start.hashCode());
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
		if (end == null) {
			if (other.end != null) {
				return false;
			}
		} else if (!end.equals(other.end)) {
			return false;
		}
		if (expression == null) {
			if (other.expression != null) {
				return false;
			}
		} else if (!expression.equals(other.expression)) {
			return false;
		}
		if (negated != other.negated) {
			return false;
		}
		if (result == null) {
			if (other.result != null) {
				return false;
			}
		} else if (!result.equals(other.result)) {
			return false;
		}
		if (start == null) {
			if (other.start != null) {
				return false;
			}
		} else if (!start.equals(other.start)) {
			return false;
		}
		return true;
	}
}
