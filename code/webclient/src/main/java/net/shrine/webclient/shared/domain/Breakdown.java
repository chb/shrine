package net.shrine.webclient.shared.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author clint
 * @author Bill Simons
 * @date Sep 10, 2012
 */
public final class Breakdown implements Map<String, Long> {

    private final Map<String, Long> delegate = new HashMap<String, Long>();

    public Breakdown() {
        super();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    public Breakdown(final Map<String, Long> values) {
        delegate.putAll(values);
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
    public Long get(final Object key) {
        return delegate.get(key);
    }

    @Override
    public Long put(final String key, final Long value) {
        return delegate.put(key, value);
    }

    @Override
    public Long remove(final Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(final Map<? extends String, ? extends Long> m) {
        delegate.putAll(m);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<String> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<Long> values() {
        return delegate.values();
    }

    @Override
    public Set<java.util.Map.Entry<String, Long>> entrySet() {
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
