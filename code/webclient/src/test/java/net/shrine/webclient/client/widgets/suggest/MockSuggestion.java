package net.shrine.webclient.client.widgets.suggest;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author clint
 * @date Apr 17, 2012
 */
public final class MockSuggestion implements IsSerializable {
	public final String suggestion;

	public MockSuggestion(final String suggestion) {
		super();
		this.suggestion = suggestion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (suggestion == null ? 0 : suggestion.hashCode());
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
		final MockSuggestion other = (MockSuggestion) obj;
		if (suggestion == null) {
			if (other.suggestion != null) {
				return false;
			}
		} else if (!suggestion.equals(other.suggestion)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "MockSuggestion [suggestion=" + suggestion + "]";
	}
}
