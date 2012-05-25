package net.shrine.webclient.client.util;

import java.util.Collection;
import java.util.Set;

/**
 * 
 * @author clint
 * @date Mar 26, 2012
 * @param <K>
 * @param <V>
 */
public interface ReadOnlyObservableMap<K, V> extends IObservable {
	int size();

	boolean isEmpty();

	boolean containsKey(final Object key);

	boolean containsValue(final Object value);

	V get(final Object key);
	
	Set<K> keySet();

	Collection<V> values();

	Set<java.util.Map.Entry<K, V>> entrySet();
}
