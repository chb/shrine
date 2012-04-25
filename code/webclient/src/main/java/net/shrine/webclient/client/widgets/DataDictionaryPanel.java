package net.shrine.webclient.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 25, 2012
 * 
 * TODO: DUMMY DATA, placeholder only
 */
public final class DataDictionaryPanel extends Composite  {

	private static DataDictionaryDataHolderUiBinder uiBinder = GWT.create(DataDictionaryDataHolderUiBinder.class);

	interface DataDictionaryDataHolderUiBinder extends UiBinder<Widget, DataDictionaryPanel> { }

	public DataDictionaryPanel() {
		initWidget(uiBinder.createAndBindUi(this));
	}
}
