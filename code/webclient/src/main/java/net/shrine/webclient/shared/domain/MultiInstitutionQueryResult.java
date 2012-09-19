package net.shrine.webclient.shared.domain;

import java.util.Map;

import net.shrine.webclient.client.util.Util;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 
 * @author clint
 * @author Bill Simons
 * @date Sep 10, 2012
 */
public class MultiInstitutionQueryResult {

    // NB: Must have default access, and not be private, to appease RestyGWT
    final Map<String, SingleInstitutionQueryResult> results = Util.makeHashMap();

    public MultiInstitutionQueryResult() {
        super();
    }

    @JsonCreator
    public MultiInstitutionQueryResult(@JsonProperty("results") final Map<String, SingleInstitutionQueryResult> results) {
        super();

        if (results != null) {
            this.results.putAll(results);
        }
    }

    public Map<String, SingleInstitutionQueryResult> asMap() {
        return getResults();
    }

    public Map<String, SingleInstitutionQueryResult> getResults() {
        return results;
    }

    @Override
    public String toString() {
        return "MultiInstitutionQueryResult [results=" + results + "]";
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
