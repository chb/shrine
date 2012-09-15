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
public class MultiInstitutionQueryResult {
    
    //NB: Must have default access, and not be private, to appease RestyGWT
    final Map<String, SingleInstitutionQueryResult> results = new HashMap<String, SingleInstitutionQueryResult>();

    public MultiInstitutionQueryResult() {
        super();
    }

    @JsonCreator
    public MultiInstitutionQueryResult(@JsonProperty("results") final Map<String, SingleInstitutionQueryResult> results) {
        super();

        if(results != null) {
            this.results.putAll(results);
        }
    }

    public Map<String, SingleInstitutionQueryResult> asMap() {
        return results;
    }

    @Override
    public String toString() {
        return results.toString();
    }

    // @Override
    public int size() {
        return results.size();
    }

    // @Override
    public boolean isEmpty() {
        return results.isEmpty();
    }

    // @Override
    public boolean containsKey(final Object key) {
        return results.containsKey(key);
    }

    // @Override
    public boolean containsValue(final Object value) {
        return results.containsValue(value);
    }

    // @Override
    public SingleInstitutionQueryResult get(final Object key) {
        return results.get(key);
    }

    // @Override
    public SingleInstitutionQueryResult put(final String key, final SingleInstitutionQueryResult value) {
        return results.put(key, value);
    }

    // @Override
    public SingleInstitutionQueryResult remove(final Object key) {
        return results.remove(key);
    }

    // @Override
    public void putAll(final Map<? extends String, ? extends SingleInstitutionQueryResult> m) {
        results.putAll(m);
    }

    // @Override
    public void clear() {
        results.clear();
    }

    // @Override
    public Set<String> keySet() {
        return results.keySet();
    }

    // @Override
    public Collection<SingleInstitutionQueryResult> values() {
        return results.values();
    }

    // @Override
    public Set<java.util.Map.Entry<String, SingleInstitutionQueryResult>> entrySet() {
        return results.entrySet();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (results == null ? 0 : results.hashCode());
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
        final MultiInstitutionQueryResult other = (MultiInstitutionQueryResult) obj;
        if (results == null) {
            if (other.results != null) {
                return false;
            }
        } else if (!results.equals(other.results)) {
            return false;
        }
        return true;
    }
}
