package net.shrine.webclient.client.util;

import java.util.Collections;
import java.util.Iterator;

/**
 * 
 * @author clint
 * @date Mar 22, 2012
 * 
 * @param <T>
 */
public final class Observable<T> extends AbstractObservable implements ReadOnlyObservable<T> {
	private T value;

	public Observable(final T value) {
		this.value = value;
	}

	public Observable() {
		this(null);
	}

	public static <T> Observable<T> empty() {
		return new Observable<T>();
	}

	public static <T> Observable<T> from(final T initialValue) {
		return new Observable<T>(initialValue);
	}

	public ReadOnlyObservable<T> readOnly() {
		return this;
	}

	public void clear() {
		set(null);
	}

	@Override
	public T get() {
		Util.requireNotNull(this.value);
		
		return this.value;
	}

	public void set(final T newValue) {
		try {
			this.value = newValue;
		} finally {
			notifyObservers();
		}
	}

	@Override
	public boolean isDefined() {
		return this.value != null;
	}

	@Override
	public boolean isEmpty() {
		return !isDefined();
	}

	@Override
	public T getOrElse(final T defaultValue) {
		if (isDefined()) {
			return get();
		} else {
			return defaultValue;
		}
	}

	@Override
	public Iterator<T> iterator() {
		if(isEmpty()) {
			return Collections.<T>emptyList().iterator();
		}
		
		return Collections.singleton(value).iterator();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (value == null ? 0 : value.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
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
		final Observable<T> other = (Observable<T>) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}
}
