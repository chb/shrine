package net.shrine.webclient.client.widgets.suggest;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author clint
 * @date Apr 5, 2012
 */
public class RichSuggestRequest implements IsSerializable {
	private int limit;

	private String query;

	// For GWT
	@SuppressWarnings("unused")
	private RichSuggestRequest() {
		super();
	}

	public RichSuggestRequest(final int limit, final String query) {
		super();

		this.limit = limit;
		this.query = query;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(final int limit) {
		this.limit = limit;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(final String query) {
		this.query = query;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + limit;
		result = prime * result + (query == null ? 0 : query.hashCode());
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
		final RichSuggestRequest other = (RichSuggestRequest) obj;
		if (limit != other.limit) {
			return false;
		}
		if (query == null) {
			if (other.query != null) {
				return false;
			}
		} else if (!query.equals(other.query)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "RichSuggestRequest [limit=" + limit + ", query=" + query + "]";
	}
}
