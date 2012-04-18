package net.shrine.webclient.client.widgets.suggest;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author clint
 * @date Apr 5, 2012
 * @param <S>
 */
public interface RichSuggestCallback<S extends IsSerializable> {
	void onSuggestionsReady(final RichSuggestRequest request, final RichSuggestResponse<S> response);
}