package net.shrine.webclient.client.domain;

import java.util.Collection;

import net.shrine.webclient.client.AbstractWebclientTest;

import org.junit.Test;

/**
 * 
 * @author clint
 * @date Apr 20, 2012
 */
public class TermTestGwt extends AbstractWebclientTest {

	@Test
	public void testTerm() {
		final String path = "//foo/blah/nuh/some-path";
		final String category = "some-category";
		final String simpleName = "some-simple-name";
		
		final Term t1 = new Term(path, category, simpleName);
		
		assertEquals(path, t1.getPath());
		assertEquals(category, t1.getCategory());
		assertEquals(simpleName, t1.getSimpleName());
		
		try {
			new Term(null, category, simpleName);
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		try {
			new Term(path, null, simpleName);
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		try {
			new Term(path, category, null);
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		try {
			new Term(null, null, null);
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testGetTerms() {
		final Term t1 = term("foo");
		
		final Collection<Term> terms = t1.getTerms();
		
		assertNotNull(terms);
		assertEquals(1, terms.size());
		assertEquals(t1, terms.iterator().next());
	}

	@Test
	public void testToXmlString() {
		final Term t1 = term("foo");
		
		assertEquals("<term>foo</term>", t1.toXmlString());
	}
}
