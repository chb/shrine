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
	public int size();

	public boolean isEmpty();

	public boolean containsKey(final Object key);

	public boolean containsValue(final Object value);

	public V get(final Object key);
	
	public Set<K> keySet();

	public Collection<V> values();

	public Set<java.util.Map.Entry<K, V>> entrySet();
}
