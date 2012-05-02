package net.shrine.webclient.client;

/**
 * 
 * @author clint
 * @date May 1, 2012
 */
public final class QueryGroupId {

	public static final QueryGroupId Null = new QueryGroupId("Null");

	public final String name;

	public QueryGroupId(final String name) {
		super();

		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (name == null ? 0 : name.hashCode());
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
		final QueryGroupId other = (QueryGroupId) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "QueryGroupId [name=" + name + "]";
	}
}
