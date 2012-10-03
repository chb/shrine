package net.shrine.webclient.client.widgets.suggest;

/**
 * 
 * @author clint
 * @date Apr 11, 2012
 * @param <S>
 * 
 *            basically a side-effecting function RichSuggestionEvent<S> => void
 */
public interface SuggestRowContainer<S> extends HasHideablePopup {
    void fireSuggestionEvent(final RichSuggestionEvent<S> event);
}
