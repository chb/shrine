package net.shrine.webclient.client;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * 
 * @author clint
 * @date Apr 11, 2012
 */
public abstract class AbstractWebclientTest extends GWTTestCase {
	@Override
	public String getModuleName() {
		return "net.shrine.webclient.WebclientJUnit";
	}
	
	protected QueryGroupId id(final String name) {
		return new QueryGroupId(name);
	}
}
