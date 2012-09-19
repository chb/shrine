package net.shrine.webclient.shared.domain;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 
 * @author clint
 * @author Bill Simons
 * @date Sep 10, 2012
 */
public final class SingleInstitutionQueryResult {
    @JsonProperty
    private final long count;

    @JsonProperty
    private final Map<String, Breakdown> breakdowns;

    @JsonProperty
    final boolean isError; 
    
    @JsonCreator
    public SingleInstitutionQueryResult(@JsonProperty("count") final long count, @JsonProperty("breakdowns") final Map<String, Breakdown> breakdowns, @JsonProperty("isError") final boolean isError) {
        super();
        this.count = count;
        this.breakdowns = new HashMap<String, Breakdown>(breakdowns);
        this.isError = isError;
    }

    public long getCount() {
        return count;
    }

    public Map<String, Breakdown> getBreakdowns() {
        return breakdowns;
    }
    
    public boolean isError() {
        return isError;
    }

    @Override
    public String toString() {
        return "SingleInstitutionQueryResult [count=" + count + ", breakdowns=" + breakdowns + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (breakdowns == null ? 0 : breakdowns.hashCode());
        result = prime * result + (int) (count ^ count >>> 32);
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
        final SingleInstitutionQueryResult other = (SingleInstitutionQueryResult) obj;
        if (breakdowns == null) {
            if (other.breakdowns != null) {
                return false;
            }
        } else if (!breakdowns.equals(other.breakdowns)) {
            return false;
        }
        if (count != other.count) {
            return false;
        }
        return true;
    }
}
