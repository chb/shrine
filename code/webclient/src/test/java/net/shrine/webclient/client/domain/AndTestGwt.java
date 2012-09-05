package net.shrine.webclient.client.domain;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.shrine.webclient.client.AbstractWebclientTest;
import net.shrine.webclient.shared.domain.And;
import net.shrine.webclient.shared.domain.Andable;
import net.shrine.webclient.shared.domain.Or;
import net.shrine.webclient.shared.domain.Term;

import org.junit.Test;

/**
 * 
 * @author clint
 * @date Apr 20, 2012
 */
public class AndTestGwt extends AbstractWebclientTest {

	@Test
	public void testAndCollectionOfQextendsExpression() {
		final And empty = new And(Collections.<Term>emptyList());
		
		assertTrue(empty.getComponents().isEmpty());
		
		{
			final List<Term> terms = asList(term("foo"), term("bar"));
			
			final And and = new And(terms);
			
			assertNotSame(terms, and.getComponents());
			assertEquals(terms, and.getComponents());
		}
		
		try {
			final Collection<Term> terms = null;
			
			new And(terms);
		} catch(IllegalArgumentException expected) { }
	}

	@Test
	public void testAndExpressionArray() {
		final And empty = new And();
		
		assertTrue(empty.getComponents().isEmpty());
		
		{
			final Term t1 = term("foo");
			final Term t2 = term("bar");
			
			final List<Term> terms = asList(t1, t2);
			
			final And and = new And(t1, t2);
			
			assertEquals(terms, and.getComponents());
		}
	}

	@Test
	public void testGetComponents() {
		final Term t1 = term("foo");
		final Term t2 = term("bar");
		
		final And and = new And(t1, t2);
		
		final List<Term> terms = asList(t1, t2);
		
		final Collection<Andable> comps1 = and.getComponents();
		final Collection<Andable> comps2 = and.getComponents();
		
		assertEquals(terms, comps1);
		assertEquals(terms, comps2);
		
		assertNotSame(comps1, comps2);
	}

	@Test
	public void testGetTerms() {
		final Term t1 = term("foo");
		final Term t2 = term("bar");
		final Term t3 = term("baz");
		final Term t4 = term("nuh");
		
		{
			final And and = new And(t1, t2);
			
			final List<Term> terms = asList(t1, t2);
			
			assertEquals(terms, and.getTerms());
		}

		{
			final And and = new And(new Or(t1, t2), new Or(t3, t4));
			
			assertEquals(asList(t1, t2, t3, t4), and.getTerms());
		}
	}

	@Test
	public void testWith() {
		final Term t1 = term("foo");
		final Term t2 = term("bar");
		
		final And and0 = new And();
		
		assertTrue(and0.getComponents().isEmpty());
		
		final And and1 = and0.with(t1);
		
		assertNotSame(and0, and1);
		
		assertTrue(and0.getComponents().isEmpty());
		assertEquals(asList(t1), and1.getComponents());
		
		final And and2 = and1.with(t2);
		
		assertNotSame(and1, and2);
		
		assertTrue(and0.getComponents().isEmpty());
		assertEquals(asList(t1), and1.getComponents());
		assertEquals(asList(t1, t2), and2.getComponents());
	}

	@Test
	public void testWithout() {
		final Term t1 = term("foo");
		final Term t2 = term("bar");
		
		final And and2 = new And(t1, t2);
		
		assertEquals(asList(t1, t2), and2.getComponents());
		
		final And and1 = and2.without(t1);
		
		assertNotSame(and1, and2);
		
		assertEquals(asList(t1, t2), and2.getComponents());
		assertEquals(asList(t2), and1.getComponents());
		
		final And and0 = and1.without(t2);
		
		assertNotSame(and0, and1);
		
		assertTrue(and0.getComponents().isEmpty());
		assertEquals(asList(t1, t2), and2.getComponents());
		assertEquals(asList(t2), and1.getComponents());
	}

	@Test
	public void testToXmlString() {
		final Term t1 = term("foo");
		final Term t2 = term("bar");
		
		final And and = new And(t1, t2);
		
		assertEquals("<and><term>foo</term><term>bar</term></and>", and.toXmlString());
	}
}
