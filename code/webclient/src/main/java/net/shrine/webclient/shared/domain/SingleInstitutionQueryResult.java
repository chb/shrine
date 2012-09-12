package net.shrine.webclient.shared.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author clint
 * @author Bill Simons
 * @date Sep 10, 2012
 */
public final class SingleInstitutionQueryResult {
    // Must be public for RestyGWt
    public long count;

    // Must be public for RestyGWt
    public HashMap<String, Breakdown> breakdowns;

    public SingleInstitutionQueryResult() {
        super();
    }

    // For tests
    public SingleInstitutionQueryResult(final long count, final Map<String, Breakdown> breakdowns) {
        super();
        this.count = count;
        this.breakdowns = new HashMap<String, Breakdown>(breakdowns);
    }

    public long getCount() {
        return count;
    }

    public void setCount(final long count) {
        this.count = count;
    }

    public Map<String, Breakdown> getBreakdowns() {
        return breakdowns;
    }

    public void setBreakdowns(final Map<String, Breakdown> breakdowns) {
        this.breakdowns.clear();
      
        if (breakdowns != null) {
            this.breakdowns.putAll(breakdowns);
        }
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
