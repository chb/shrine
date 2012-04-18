package net.shrine.webclient.client.widgets.suggest;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author clint
 * @date Apr 5, 2012
 */
public interface RichSuggestOracle<S extends IsSerializable> {
	void requestSuggestions(final RichSuggestRequest request, final RichSuggestCallback<S> callback);
}
