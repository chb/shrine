package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.util.Util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 25, 2012
 */
public final class InstitutionResult extends Composite {

	private static InstitutionResultUiBinder uiBinder = GWT.create(InstitutionResultUiBinder.class);

	interface InstitutionResultUiBinder extends UiBinder<Widget, InstitutionResult> { }

	@UiField
	HTML delegate;
	
	public InstitutionResult(final String instName, final int resultSetSize) {
		super();
		
		Util.requireNotNull(instName);
		
		initWidget(uiBinder.createAndBindUi(this));
		
		delegate.setHTML(instName + ": <strong>" + resultSetSize + "</strong>");
	}
}
