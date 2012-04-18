package net.shrine.webclient.client.util;

/**
 * 
 * @author clint
 * @date Mar 22, 2012
 */
public interface Observer {
	void inform();
	
	void stopObserving();
}
