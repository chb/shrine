package net.shrine.webclient.client.controllers;

import net.shrine.webclient.client.services.QueryService;
import net.shrine.webclient.client.state.State;


/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
public final class Controllers {
    public final QueryConstraintController constraints;
    
    public final QueryBuildingController queryBuilding;
    
    public final QueryController query;
    
    public Controllers(final State state, final QueryService queryService) {
    	super();
    	
    	this.query = new QueryController(state, queryService);
    	
    	this.constraints = new QueryConstraintController(state);
    	
    	this.queryBuilding = new QueryBuildingController(state);
    }
}
