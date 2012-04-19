package net.shrine.webclient.client.domain;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author clint
 * @date Apr 4, 2012
 * 
 * Work around GWT-RPC's need for a default constructor :( :(
 */
public final class IntWrapper implements IsSerializable {
	private int value;

	// For GWT :(
	@SuppressWarnings("unused")
	private IntWrapper() {
		this(0);
	}

	public IntWrapper(final int value) {
		super();

		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(final int value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value;
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
		final IntWrapper other = (IntWrapper) obj;
		if (value != other.value) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
