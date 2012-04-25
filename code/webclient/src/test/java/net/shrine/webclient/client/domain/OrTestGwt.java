package net.shrine.webclient.client.domain;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.List;

import net.shrine.webclient.client.AbstractWebclientTest;

import org.junit.Test;

/**
 * 
 * @author clint
 * @date Apr 25, 2012
 */
public class OrTestGwt extends AbstractWebclientTest {

	private final Term t1 = new Term("foo");
	private final Term t2 = new Term("bar");
	private final Term t3 = new Term("nuh");
	private final Term t4 = new Term("zuh");
	
	@Test
	public void testOrCollectionOfTerm() {
		final List<Term> terms = asList(t1, t2, t3, t4);
		
		final Or or = new Or(terms);
		
		assertEquals(terms, or.getTerms());
		assertNotSame(terms, or.getTerms());
		
		try {
			new Or((Collection<Term>)null);
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testOrTermArray() {
		final Or or = new Or(t1, t2, t3);
		
		assertEquals(asList(t1, t2, t3), or.getTerms());
		
		final Or empty = new Or();
		
		assertTrue(empty.getTerms().isEmpty());
	}

	@Test
	public void testWith() {
		final Or empty = new Or();
		
		assertTrue(empty.getTerms().isEmpty());
		
		final Or or1 = empty.with(t1);
		
		assertNotSame(empty, or1);
		
		assertTrue(empty.getTerms().isEmpty());
		assertEquals(asList(t1), or1.getTerms());
		
		final Or or2 = or1.with(t2);
		
		assertNotSame(or1, or2);
		
		assertTrue(empty.getTerms().isEmpty());
		assertEquals(asList(t1), or1.getTerms());
		assertEquals(asList(t1, t2), or2.getTerms());
	}

	@Test
	public void testWithout() {
		final Or or2 = new Or(t1, t2);
		
		assertEquals(asList(t1, t2), or2.getTerms());
		
		final Or or1 = or2.without(t1);
		
		assertNotSame(or1, or2);
		assertEquals(asList(t2), or1.getTerms());
		assertEquals(asList(t1, t2), or2.getTerms());
		
		final Or empty = or1.without(t2);
		
		assertNotSame(or1, empty);
		assertEquals(asList(t2), or1.getTerms());
		assertEquals(asList(t1, t2), or2.getTerms());
		assertTrue(empty.getTerms().isEmpty());
	}

	@Test
	public void testToXmlString() {
		assertEquals("<or></or>", new Or().toXmlString());
		
		assertEquals(t1.toXmlString(), new Or(t1).toXmlString());
		
		assertEquals("<or>" + t3.toXmlString() + t4.toXmlString() + "</or>", new Or(t3, t4).toXmlString());
	}
}
