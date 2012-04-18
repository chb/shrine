package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.util.ReadOnlyObservable;
import net.shrine.webclient.client.util.Util;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * 
 * @author clint
 * @date Apr 4, 2012
 */
public final class ResultToolTipRow extends FlowPanel {

	public ResultToolTipRow(final String institutionName, final ReadOnlyObservable<Integer> result) {
		Util.requireNotNull(institutionName);
		Util.requireNotNull(result);
		
		if(result.isDefined()) {
			//TODO: TOTAL HACK: WhyTF do I have to make all spaces non-breaking?? 
			this.getElement().setInnerHTML("<span>" + institutionName.replaceAll("\\s", "&nbsp;") + ":&nbsp;<strong>" + result.get() + "</strong></span><br>");
		} else {
			this.add(new Label(institutionName + ": "));
			this.add(new LoadingSpinner());
		}
	}
}
