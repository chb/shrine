package net.shrine.webclient.shared.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * 
 * @author clint
 * @author Bill Simons
 * @date Sep 10, 2012
 */
@JsonSerialize(as = Map.class)
public final class Breakdown {

    //NB: Must have default access, and not be private, to appease RestyGWT
    final Map<String, Long> breakdown = new HashMap<String, Long>();

    public Breakdown() {
        super();
    }

    @JsonCreator
    public Breakdown(@JsonProperty("breakdown") final Map<String, Long> breakdown) {
        super();
        
        if(breakdown != null) {
            this.breakdown.putAll(breakdown);
        }
    }
    
    public Map<String, Long> asMap() {
        return breakdown;
    }
    
    @Override
    public String toString() {
        return "Breakdown [breakdown=" + breakdown + "]";
    }

    //@Override
    public int size() {
        return breakdown.size();
    }

    //@Override
    public boolean isEmpty() {
        return breakdown.isEmpty();
    }

    //@Override
    public boolean containsKey(final Object key) {
        return breakdown.containsKey(key);
    }

    //@Override
    public boolean containsValue(final Object value) {
        return breakdown.containsValue(value);
    }

    //@Override
    public Long get(final Object key) {
        return breakdown.get(key);
    }

    //@Override
    public Long put(final String key, final Long value) {
        return breakdown.put(key, value);
    }

    //@Override
    public Long remove(final Object key) {
        return breakdown.remove(key);
    }

    //@Override
    public void putAll(final Map<? extends String, ? extends Long> m) {
        breakdown.putAll(m);
    }

    //@Override
    public void clear() {
        breakdown.clear();
    }

    //@Override
    public Set<String> keySet() {
        return breakdown.keySet();
    }

    //@Override
    public Collection<Long> values() {
        return breakdown.values();
    }

    //@Override
    public Set<java.util.Map.Entry<String, Long>> entrySet() {
        return breakdown.entrySet();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (breakdown == null ? 0 : breakdown.hashCode());
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
        final Breakdown other = (Breakdown) obj;
        if (breakdown == null) {
            if (other.breakdown != null) {
                return false;
            }
        } else if (!breakdown.equals(other.breakdown)) {
            return false;
        }
        return true;
    }
}
