package net.shrine.webclient.client.widgets.suggest;

import java.util.Collection;
import java.util.Collections;

import net.shrine.webclient.client.util.Util;

/**
 * 
 * @author clint
 * @date Apr 5, 2012
 */
public class RichSuggestResponse<S> {

    private Collection<S> suggestions;

    private int sequenceNumber;

    // For GWT
    @SuppressWarnings("unused")
    private RichSuggestResponse() {
        this(Util.<S> makeArrayList(), 0);
    }

    public RichSuggestResponse(final Collection<S> suggestions, final int sequenceNumber) {
        super();

        this.suggestions = suggestions;
        this.sequenceNumber = sequenceNumber;
    }

    public static final <S> RichSuggestResponse<S> of(final Collection<S> suggestions, final int sequenceNumber) {
        return new RichSuggestResponse<S>(suggestions, sequenceNumber);
    }

    public static final <S> RichSuggestResponse<S> empty() {
        return new RichSuggestResponse<S>(Collections.<S> emptyList(), 0);
    }

    public Collection<S> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(final Collection<S> suggestions) {
        this.suggestions = suggestions;
    }

    public boolean hasSuggestions() {
        return !suggestions.isEmpty();
    }
    
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + sequenceNumber;
        result = prime * result + (suggestions == null ? 0 : suggestions.hashCode());
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
        final RichSuggestResponse<?> other = (RichSuggestResponse<?>) obj;
        if (sequenceNumber != other.sequenceNumber) {
            return false;
        }
        if (suggestions == null) {
            if (other.suggestions != null) {
                return false;
            }
        } else if (!suggestions.equals(other.suggestions)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RichSuggestResponse [suggestions=" + suggestions + ", sequenceNumber=" + sequenceNumber + "]";
    }
}
