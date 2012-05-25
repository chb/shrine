package net.shrine.webclient.client.controllers;

import net.shrine.webclient.client.state.State;
import net.shrine.webclient.client.util.Util;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
public abstract class StatefulController {

	protected final State state;

	public StatefulController(final State state) {
		super();
		
		Util.requireNotNull(state);
		
		this.state = state;
	}
}
