package net.shrine.webclient.client.widgets;

import net.shrine.webclient.client.Controllers;
import net.shrine.webclient.client.domain.OntNode;
import net.shrine.webclient.client.events.CollapseDataDictionaryPanelEvent;
import net.shrine.webclient.client.util.Util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author clint
 * @date Apr 4, 2012
 */
public final class OntTreeNode extends Composite {

	private static OntTreeNodeUiBinder uiBinder = GWT.create(OntTreeNodeUiBinder.class);

	interface OntTreeNodeUiBinder extends UiBinder<Widget, OntTreeNode> { }

	static final String selectedStyleName = "gwt-TreeItem-selected";
	
	@UiField
	SpanElement iconSpan;
	
	@UiField
	SpanElement spacer;
	
	@UiField
	Anchor textAnchor;
	
	public OntTreeNode(final EventBus eventBus, final Controllers controllers, final OntNode node ) {
		initWidget(uiBinder.createAndBindUi(this));
		
		Util.requireNotNull(node);
		
		textAnchor.setText(node.getSimpleName());
		iconSpan.setClassName(node.isLeaf() ? "leaf" : "folder");
		
		iconSpan.setTitle(node.getValue());
		textAnchor.setTitle(node.getValue());
		
		//TODO: HACK ALERT
		if(node.isLeaf()) {
			spacer.setInnerHTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		}
		//END HACK ALERT
		
		textAnchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				controllers.queryBuilding.addNewTerm(node.toTerm());
				
				eventBus.fireEvent(CollapseDataDictionaryPanelEvent.Instance);
			}
		});
	}
	
	public void select() {
		this.addStyleName(selectedStyleName);
		
		iconSpan.addClassName(selectedStyleName);
		spacer.addClassName(selectedStyleName);
		textAnchor.addStyleName(selectedStyleName);
	}
	
	public void deselect() {
		this.removeStyleName(selectedStyleName);
		
		iconSpan.removeClassName(selectedStyleName);
		spacer.removeClassName(selectedStyleName);
		textAnchor.removeStyleName(selectedStyleName);
	}
}
