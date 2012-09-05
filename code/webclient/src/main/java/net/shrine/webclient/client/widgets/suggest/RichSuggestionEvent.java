package net.shrine.webclient.client.widgets.suggest;

import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * @author clint
 * @date Apr 5, 2012
 */
public final class RichSuggestionEvent<S> extends GwtEvent<RichSuggestionEventHandler<S>> {

    // NB: This must be a raw type to make cast in getAssociatedType() work.
    // Because GwtEvent.Type is stateless, we can safely cast a singleton here.
    @SuppressWarnings("rawtypes")
    private static final GwtEvent.Type TYPE = new GwtEvent.Type();

    // NB: must return the singleton TYPE instance; returning a new instance of
    // GwtEvent.Type<RichSuggestionEventHandler<S>> made handlers not get
    // called. :(
    @SuppressWarnings("unchecked")
    public static <S> GwtEvent.Type<RichSuggestionEventHandler<S>> getType() {
        return TYPE;
    }

    private final S suggestion;

    RichSuggestionEvent() {
        this(null);
    }

    RichSuggestionEvent(final S suggestion) {
        super();

        this.suggestion = suggestion;
    }

    public static <S> RichSuggestionEvent<S> from(final S suggestion) {
        return new RichSuggestionEvent<S>(suggestion);
    }

    public S getSuggestion() {
        return suggestion;
    }

    // NB: must return the singleton TYPE instance; returning a new instance of
    // GwtEvent.Type<RichSuggestionEventHandler<S>> made handlers not get
    // called. :(
    @Override
    public GwtEvent.Type<RichSuggestionEventHandler<S>> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(final RichSuggestionEventHandler<S> handler) {
        handler.onSelectionMade(this);
    }

    @Override
    public String toString() {
        return "RichSuggestionEvent [suggestion=" + suggestion + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (suggestion == null ? 0 : suggestion.hashCode());
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
        final RichSuggestionEvent<?> other = (RichSuggestionEvent<?>) obj;
        if (suggestion == null) {
            if (other.suggestion != null) {
                return false;
            }
        } else if (!suggestion.equals(other.suggestion)) {
            return false;
        }
        return true;
    }
}
