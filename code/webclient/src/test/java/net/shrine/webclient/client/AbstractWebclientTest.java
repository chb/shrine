package net.shrine.webclient.client;

import net.shrine.webclient.client.domain.Term;

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
	
	protected Term term(final String path) {
		return new Term(path, "some-bogus-category");
	}
}
