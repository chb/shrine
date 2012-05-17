package net.shrine.webclient.client.widgets;

import java.util.List;

import net.shrine.webclient.client.Controllers;
import net.shrine.webclient.client.OntologySearchService;
import net.shrine.webclient.client.OntologySearchServiceAsync;
import net.shrine.webclient.client.domain.OntNode;
import net.shrine.webclient.client.util.Util;

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
	
	public OntologyTree(final EventBus eventBus, final Controllers controllers, final OntNode shownNode, final List<OntNode> pathFromRoot) {
		super();
		
		Util.requireNotNull(eventBus);
		Util.requireNotNull(controllers);
		
		this.controllers = controllers;
		this.eventBus = eventBus;
		
		delegate = makeTree(shownNode, pathFromRoot);
		
		initWidget(delegate);
	}

	private final class OntTreeItem extends TreeItem {

		boolean shouldGetChildrenFromServer;
		
		final OntNode ontNode;
		
		OntTreeItem(final OntNode ontNode) {
			this(ontNode, false);
		}
		
		OntTreeItem(final OntNode ontNode, final boolean shouldGetChildrenFromServer) {
			super(new OntTreeNode(eventBus, controllers, ontNode));
			
			this.shouldGetChildrenFromServer = shouldGetChildrenFromServer;
			
			this.ontNode = ontNode;
		}
	}
	
	private final TreeItem dummyItem() {
		return new TreeItem("");
	}
	
	private Tree makeTree(final OntNode shownNode, final List<OntNode> pathFromRoot) {
		Log.debug("Making tree: shown node: " + shownNode);
		Log.debug("Path from root: " + pathFromRoot);
		
		final Tree tree = new Tree();
		
		addOpenHandler(tree);
		
		final OntTreeItem rootItem;
		
		OntTreeItem cursor;
		
		if(pathFromRoot.isEmpty()) {
			cursor = rootItem = new OntTreeItem(shownNode);
		} else {
			cursor = rootItem = new OntTreeItem(new OntNode(DataDictionaryRow.shrineRoot, false), true);
			
			for(final OntNode descendant : pathFromRoot) {
				final OntTreeItem descendantItem = new OntTreeItem(descendant, true);
				
				cursor.addItem(descendantItem);
				
				cursor.setState(true, false);
				
				cursor = descendantItem;
			}
		}
		
		tree.addItem(rootItem);
		
		if(cursor != rootItem) {
			makeOpenable(cursor, true, true);
			
		} else if(rootItem.getChildCount() < 1) {
			final boolean shouldFetchRootItemsChildren = pathFromRoot.isEmpty();
			
			makeOpenable(rootItem, true, shouldFetchRootItemsChildren);
		}
		
		return tree;
	}
	
	void makeOpenable(final OntTreeItem item, final boolean open, final boolean fireEvents) {
		item.setState(false, false);
		
		item.addItem(dummyItem());
	
		item.setState(open, fireEvents);
	}

	void addOpenHandler(final Tree tree) {
		tree.addOpenHandler(new OpenHandler<TreeItem>() {
			@Override
			public void onOpen(final OpenEvent<TreeItem> event) {
				final OntTreeItem item = (OntTreeItem)event.getTarget();
				
				//If we haven't been opened (only have the dummy item)
				if(shouldLoadChildren(item)) {
					item.removeItems();
					
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

			boolean shouldLoadChildren(final OntTreeItem item) {
				return item.shouldGetChildrenFromServer || hasOnlyDummyChild(item);
			}

			boolean hasOnlyDummyChild(final OntTreeItem item) {
				return hasOnlyOneChild(item) && hasDummyChild(item);
			}

			boolean hasOnlyOneChild(final OntTreeItem item) {
				return item.getChildCount() == 1;
			}

			boolean hasDummyChild(final OntTreeItem item) {
				return item.getChild(0).getText().equals("");
			}
		});
	}
}
