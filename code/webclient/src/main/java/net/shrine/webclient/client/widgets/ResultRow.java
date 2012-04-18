package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.util.Observer;
import net.shrine.webclient.client.util.ReadOnlyObservable;
import net.shrine.webclient.client.util.Util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 4, 2012
 */
public final class ResultRow extends Composite implements Observer {

	private static ResultRowUiBinder uiBinder = GWT.create(ResultRowUiBinder.class);

	interface ResultRowUiBinder extends UiBinder<Widget, ResultRow> { }

	@UiField
	SpanElement institutionLabel;
	
	@UiField
	SpanElement resultPanel;
	
	private final ReadOnlyObservable<Integer> result;
	
	public ResultRow(final String institutionName, final ReadOnlyObservable<Integer> result) {
		initWidget(uiBinder.createAndBindUi(this));
		
		Util.requireNotNull(institutionName);
		Util.requireNotNull(result);
		
		institutionLabel.setInnerText(institutionName);
		
		this.result = result;
		
		this.result.observedBy(this);
		
		this.inform();
	}

	@Override
	public void inform() {
		if(result.isDefined()) {
			resultPanel.setInnerText(WidgetUtil.textFor(result));
		} else {
			resultPanel.setInnerHTML((new LoadingSpinner()).toString());
		}
	}

	@Override
	public void stopObserving() {
		result.noLongerObservedBy(this);
	}
}
