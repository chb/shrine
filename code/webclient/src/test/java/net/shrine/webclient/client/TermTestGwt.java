package net.shrine.webclient.client;

import static org.junit.Assert.fail;

import java.util.Collection;

import net.shrine.webclient.client.domain.Term;

import org.junit.Test;

/**
 * 
 * @author clint
 * @date Apr 20, 2012
 */
public class TermTestGwt extends AbstractWebclientTest {

	@Test
	public void testTerm() {
		final Term t1 = new Term("foo");
		
		assertEquals("foo", t1.value);
		
		try {
			new Term(null);
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testGetTerms() {
		final Term t1 = new Term("foo");
		
		final Collection<Term> terms = t1.getTerms();
		
		assertNotNull(terms);
		assertEquals(1, terms.size());
		assertEquals(t1, terms.iterator().next());
	}

	@Test
	public void testToXmlString() {
		final Term t1 = new Term("foo");
		
		assertEquals("<term>foo</term>", t1.toXmlString());
	}
}
