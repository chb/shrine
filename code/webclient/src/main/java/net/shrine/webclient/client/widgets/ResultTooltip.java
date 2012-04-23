package net.shrine.webclient.client.widgets;

import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

import net.shrine.webclient.client.domain.IntWrapper;
import net.shrine.webclient.client.util.Observable;
import net.shrine.webclient.client.util.ReadOnlyObservable;
import net.shrine.webclient.client.util.Util;

/**
 * 
 * @author clint
 * @date Apr 4, 2012
 */
public final class ResultTooltip extends Composite {

	private final FlowPanel delegate = new FlowPanel();
	
	//NB: Exposed for testing
	final HashMap<String, IntWrapper> breakDown;
	
	public ResultTooltip(final HashMap<String, IntWrapper> breakDown) {
		super();
		
		Util.requireNotNull(breakDown);
		
		this.breakDown = breakDown;
		
		initWidget(delegate);
		
		delegate.getElement().setId("tooltip");
		
		//TODO: TOTAL HACK: use string concatenation to make tooltip look passable for demo
		final StringBuilder toolTipHtml = new StringBuilder();
		
		for(final Entry<String, IntWrapper> entry : breakDown.entrySet()) {
			final String instName = entry.getKey();
			final ReadOnlyObservable<Integer> count = Observable.from(entry.getValue().getValue());
			
			final ResultToolTipRow resultToolTipRow = new ResultToolTipRow(instName, count);
			
			toolTipHtml.append(resultToolTipRow.getElement().getInnerHTML());
			
			//delegate.add(resultToolTipRow);
		}
		
		delegate.getElement().setInnerHTML(toolTipHtml.toString());
	}
}
