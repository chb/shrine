package net.shrine.webclient.client.widgets.suggest;

/**
 * 
 * @author clint
 * @date Apr 5, 2012
 * @param <S>
 */
public interface RichSuggestCallback<S> {
    void onSuggestionsReady(final RichSuggestRequest request, final RichSuggestResponse<S> response);
}