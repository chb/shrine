package net.shrine.webclient.client.widgets;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import net.shrine.webclient.client.AbstractWebclientTest;
import net.shrine.webclient.client.Controllers;
import net.shrine.webclient.client.MockQueryServiceAsync;
import net.shrine.webclient.client.State;
import net.shrine.webclient.client.domain.And;
import net.shrine.webclient.client.domain.IntWrapper;
import net.shrine.webclient.client.domain.Or;
import net.shrine.webclient.client.domain.QueryGroup;
import net.shrine.webclient.client.domain.Term;
import net.shrine.webclient.client.util.Observable;

import org.junit.Test;

import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * 
 * @author clint
 * @date Apr 23, 2012
 */
public class QueryRowTestGwt extends AbstractWebclientTest {

	@Test
	public void testConstructorGuards() {
		final MockQueryServiceAsync mockQueryService = new MockQueryServiceAsync(); 
		
		try {
			new QueryRow(null, "", new QueryGroup(new Term("foo"), Observable.<HashMap<String, IntWrapper>> empty()));

			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }

		try {
			new QueryRow(new Controllers(new State(), mockQueryService), null, new QueryGroup(new Term("foo"), Observable.<HashMap<String, IntWrapper>> empty()));

			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }

		try {
			new QueryRow(new Controllers(new State(), mockQueryService), "", null);

			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }

		try {
			new QueryRow(null, null, null);

			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }

		try {
			new QueryRow(new Controllers(new State(), mockQueryService), "", new QueryGroup(null, Observable.<HashMap<String, IntWrapper>> empty()));

			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }

		try {
			new QueryRow(new Controllers(new State(), mockQueryService), "", new QueryGroup(new Term("foo"), null));

			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }

		try {
			new QueryRow(new Controllers(new State(), mockQueryService), "", new QueryGroup(null, null));

			fail("Should have thrown");
		} catch (IllegalArgumentException expected) { }
	}

	@Test
	public void testQueryRow() throws Exception {
		final Controllers controllers = new Controllers(new State(), new MockQueryServiceAsync());

		final Observable<HashMap<String, IntWrapper>> result = Observable.<HashMap<String, IntWrapper>> empty();

		final QueryRow queryRow = new QueryRow(controllers, "foo", new QueryGroup(new Term("foo"), result));

		//Test negate box init
		{
			assertFalse(queryRow.negationCheckbox.getValue());

			final QueryGroup group2 = new QueryGroup(new Term("foo"), result);
			
			group2.setNegated(true);
			
			final QueryRow row2 = new QueryRow(controllers, "foo", group2);
			
			assertTrue(row2.negationCheckbox.getValue());
		}
		
		//test date box inits
		{
			assertNull(queryRow.startDate.getValue());
			assertNull(queryRow.endDate.getValue());

			final QueryGroup group2 = new QueryGroup(new Term("foo"), result);

			final Date start = new Date();
			final Date end = new Date();
			
			group2.setStart(start);
			group2.setEnd(end);
			
			final QueryRow row2 = new QueryRow(controllers, "foo", group2);
			
			assertEquals(trim(start), row2.startDate.getValue());
			assertEquals(trim(end), row2.endDate.getValue());
		}
		
		//test min occurs spinner init
		{
			assertEquals(1, queryRow.minOccursSpinner.getValue());

			final QueryGroup group2 = new QueryGroup(new Term("foo"), result);
			
			group2.setMinOccurances(99);
			
			final QueryRow row2 = new QueryRow(controllers, "foo", group2);
			
			assertEquals(99, row2.minOccursSpinner.getValue());
		}
	}

	private Date trim(final Date date) throws Exception {
		final DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyyMMdd");
		
		return dateFormat.parse(dateFormat.format(date));
	}

	@Test
	public void testInform() {
		final Controllers controllers = new Controllers(new State(), new MockQueryServiceAsync());

		final Observable<HashMap<String, IntWrapper>> result = Observable.<HashMap<String, IntWrapper>> empty();

		final QueryRow queryRow = new QueryRow(controllers, "foo", new QueryGroup(new Term("foo"), result));
		
		assertFalse(queryRow.negationCheckbox.getValue());
		assertNull(queryRow.startDate.getValue());
		assertNull(queryRow.endDate.getValue());
		assertEquals(1, queryRow.minOccursSpinner.getValue());
	}

	@Test
	public void testMakeQueryTermsFrom() {
		final Controllers controllers = new Controllers(new State(), new MockQueryServiceAsync());

		final Observable<HashMap<String, IntWrapper>> result = Observable.<HashMap<String, IntWrapper>> empty();

		final QueryRow queryRow = new QueryRow(controllers, "foo", new QueryGroup(new Term("foo"), result));
		
		try {
			queryRow.makeQueryTermsFrom(null);
			
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		try {
			queryRow.makeQueryTermsFrom(new And());
			
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		final Term t1 = new Term("foo");
		final Term t2 = new Term("foo");
		final Term t3 = new Term("foo");
		
		{
			final Collection<QueryTerm> terms = queryRow.makeQueryTermsFrom(t1);
			
			assertEquals(1, terms.size());
			
			assertEquals(t1.simpleName, terms.iterator().next().termSpan.getInnerText());
			assertEquals(t1.value, terms.iterator().next().getTitle());
		}
		
		{
			final Collection<QueryTerm> terms = queryRow.makeQueryTermsFrom(new Or(t2, t3));
			
			assertEquals(2, terms.size());
			
			final Iterator<QueryTerm> iterator = terms.iterator();
			
			final QueryTerm queryTerm1 = iterator.next();
			final QueryTerm queryTerm2 = iterator.next();
			
			assertEquals(t2.simpleName, queryTerm1.termSpan.getInnerText());
			assertEquals(t2.value, queryTerm1.getTitle());
			
			assertEquals(t3.simpleName, queryTerm2.termSpan.getInnerText());
			assertEquals(t3.value, queryTerm2.getTitle());
		}
	}
}
