package net.shrine.webclient.client;

import com.google.gwt.core.client.GWT;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
public final class Controllers {
    public final QueryConstraintController constraints;
    
    public final QueryBuildingController queryBuilding;
    
    public final QueryController query;
    
    public Controllers(final State state) {
    	super();
    	
    	this.query = new QueryController(state, GWT.<QueryServiceAsync>create(QueryService.class));
    	
    	this.constraints = new QueryConstraintController(state);
    	
    	this.queryBuilding = new QueryBuildingController(state);
    }
}
