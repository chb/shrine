package net.shrine.webclient.client.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 * @param <K>
 * @param <V>
 */
public final class ObservableMap<K, V> extends AbstractObservable implements Map<K, V>, ReadOnlyObservableMap<K, V> {
	private final Map<K, V> delegate;

	public ObservableMap(final Map<K, V> delegate) {
		super();

		Util.requireNotNull(delegate);

		this.delegate = delegate;
	}

	public static <K, V> ObservableMap<K, V> empty() {
		return new ObservableMap<K, V>(new HashMap<K, V>());
	}

	public ReadOnlyObservableMap<K, V> readOnly() {
		return this;
	}
	
	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean containsKey(final Object key) {
		return delegate.containsKey(key);
	}

	@Override
	public boolean containsValue(final Object value) {
		return delegate.containsValue(value);
	}

	@Override
	public V get(final Object key) {
		return delegate.get(key);
	}

	@Override
	public V put(final K key, final V value) {
		try {
			return delegate.put(key, value);
		} finally {
			notifyObservers();
		}
	}

	@Override
	public V remove(final Object key) {
		try {
			return delegate.remove(key);
		} finally {
			notifyObservers();
		}
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> m) {
		try {
			delegate.putAll(m);
		} finally {
			notifyObservers();
		}
	}

	@Override
	public void clear() {
		try {
			delegate.clear();
		} finally {
			notifyObservers();
		}
	}

	@Override
	public Set<K> keySet() {
		return delegate.keySet();
	}

	@Override
	public Collection<V> values() {
		return delegate.values();
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return delegate.entrySet();
	}

	@Override
	public boolean equals(final Object o) {
		return delegate.equals(o);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}
}
