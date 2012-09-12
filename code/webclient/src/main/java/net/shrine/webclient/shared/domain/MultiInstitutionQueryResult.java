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
public class MultiInstitutionQueryResult implements Map<String, SingleInstitutionQueryResult> {
    private final HashMap<String, SingleInstitutionQueryResult> delegate = new HashMap<String, SingleInstitutionQueryResult>();

    public MultiInstitutionQueryResult() {
        super();
    }

    //For tests
    public MultiInstitutionQueryResult(final Map<String, SingleInstitutionQueryResult> results) {
        super();

        delegate.putAll(results);
    }

    @Override
    public String toString() {
        return delegate.toString();
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
    public SingleInstitutionQueryResult get(final Object key) {
        return delegate.get(key);
    }

    @Override
    public SingleInstitutionQueryResult put(final String key, final SingleInstitutionQueryResult value) {
        return delegate.put(key, value);
    }

    @Override
    public SingleInstitutionQueryResult remove(final Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(final Map<? extends String, ? extends SingleInstitutionQueryResult> m) {
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
    public Collection<SingleInstitutionQueryResult> values() {
        return delegate.values();
    }

    @Override
    public Set<java.util.Map.Entry<String, SingleInstitutionQueryResult>> entrySet() {
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
