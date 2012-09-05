package net.shrine.webclient.shared.domain;

import java.util.Date;

/**
 * 
 * @author clint
 * @date Apr 27, 2012
 */
public final class PreviousQuery implements Comparable<PreviousQuery> {

    private final String queryId;

    private final Date date;

    // NB: For GWT Serialization
    @SuppressWarnings("unused")
    private PreviousQuery() {
        this(null, null);
    }

    public PreviousQuery(final String queryId, final Date date) {
        super();

        this.queryId = queryId;
        this.date = date;
    }

    @Override
    public int compareTo(final PreviousQuery o) {
        return this.date.compareTo(o.date);
    }

    public String getQueryId() {
        return queryId;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (date == null ? 0 : date.hashCode());
        result = prime * result + (queryId == null ? 0 : queryId.hashCode());
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
        final PreviousQuery other = (PreviousQuery) obj;
        if (date == null) {
            if (other.date != null) {
                return false;
            }
        } else if (!date.equals(other.date)) {
            return false;
        }
        if (queryId == null) {
            if (other.queryId != null) {
                return false;
            }
        } else if (!queryId.equals(other.queryId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PreviousQuery [" + queryId + ", " + date + "]";
    }
}
