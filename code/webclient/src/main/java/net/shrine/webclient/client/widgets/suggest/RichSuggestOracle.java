package net.shrine.webclient.client.widgets.suggest;

/**
 * 
 * @author clint
 * @date Apr 5, 2012
 */
public interface RichSuggestOracle<S> {
    void requestSuggestions(final RichSuggestRequest request, final RichSuggestCallback<S> callback);
}
