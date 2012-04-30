package net.shrine.webclient.client.widgets;

import java.util.List;

import net.shrine.webclient.client.Controllers;
import net.shrine.webclient.client.OntologySearchService;
import net.shrine.webclient.client.OntologySearchServiceAsync;
import net.shrine.webclient.client.domain.OntNode;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * 
 * @author clint
 * @date Mar 30, 2012
 */
public final class OntologyTree extends Composite {
	private final Tree delegate;
	
	private static final OntologySearchServiceAsync ontologyService = GWT.create(OntologySearchService.class);
	
	private final Controllers controllers;
	
	private final EventBus eventBus;
	
	public OntologyTree(final EventBus eventBus, final Controllers controllers, final OntNode node) {
		super();
		
		delegate = makeTree(node);
		
		this.controllers = controllers;
		this.eventBus = eventBus;
		
		initWidget(delegate);
	}

	private final class OntTreeItem extends TreeItem {

		final OntNode ontNode;
		
		OntTreeItem(final OntNode ontNode) {
			super(new OntTreeNode(eventBus, controllers, ontNode));
			
			this.ontNode = ontNode;
		}
	}
	
	private final TreeItem dummyItem() {
		return new TreeItem("");
	}
	
	private Tree makeTree(final OntNode node) {
		final Tree root = new Tree();
		
		final OntTreeItem rootItem = new OntTreeItem(node);
		
		rootItem.setState(false, false);
		
		root.addItem(rootItem);
		
		rootItem.addItem(dummyItem());
		
		root.addOpenHandler(new OpenHandler<TreeItem>() {
			@Override
			public void onOpen(final OpenEvent<TreeItem> event) {
				final OntTreeItem item = (OntTreeItem)event.getTarget();
				
				//If we haven't been opened (only have the dummy item)
				if(item.getChildCount() == 1 && item.getChild(0).getText().equals("")) {
					ontologyService.getChildrenFor(item.ontNode.getValue(), new AsyncCallback<List<OntNode>>() {
						@Override
						public void onSuccess(final List<OntNode> childNodes) {
							for(final OntNode childNode : childNodes) {
								//TODO: TOTAL HACK: skip spurious metadata
								if(!childNode.getValue().contains("SHRINE_With_Syns_08.30.10")) {
									final OntTreeItem childItem = new OntTreeItem(childNode);
									
									//If this node isn't a node, give it a dummy child so it's openable 
									if(!childNode.isLeaf()) {
										childItem.addItem(dummyItem());
									}
									
									item.addItem(childItem);
								}
							}
						}
						
						@Override
						public void onFailure(final Throwable caught) {
							Log.error("Couldn't get children of term: " + item.ontNode.getValue(), caught);
						}
					});
				}
			}
		});
		
		rootItem.setState(true);
		
		return root;
	}
}
