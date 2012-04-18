package net.shrine.webclient.client.domain;

import net.shrine.webclient.client.util.Observable;

/**
 * 
 * @author clint
 * @date Mar 26, 2012
 */
public final class Query {
	private final Expression expression;

	private final Observable<Integer> result;

	public Query(final Expression expression, final Observable<Integer> result) {
		super();
		this.expression = expression;
		this.result = result;
	}

	public Expression getExpression() {
		return expression;
	}

	public Observable<Integer> getResult() {
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (expression == null ? 0 : expression.hashCode());
		result = prime * result + (this.result == null ? 0 : this.result.hashCode());
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
		final Query other = (Query) obj;
		if (expression == null) {
			if (other.expression != null) {
				return false;
			}
		} else if (!expression.equals(other.expression)) {
			return false;
		}
		if (result == null) {
			if (other.result != null) {
				return false;
			}
		} else if (!result.equals(other.result)) {
			return false;
		}
		return true;
	}

}
