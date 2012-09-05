package net.shrine.webclient.client.state;

import static java.util.Arrays.asList;
import static net.shrine.webclient.client.state.QuerySummarizer.toLabel;
import static net.shrine.webclient.client.state.QuerySummarizer.toNegatedLabel;
import static net.shrine.webclient.client.state.QuerySummarizer.toSingularCategory;

import java.util.Collections;
import java.util.Date;

import net.shrine.webclient.client.AbstractWebclientTest;
import net.shrine.webclient.client.state.QuerySummarizer;
import net.shrine.webclient.client.state.ReadOnlyQueryGroup;
import net.shrine.webclient.client.util.Observer;
import net.shrine.webclient.shared.domain.And;
import net.shrine.webclient.shared.domain.Expression;
import net.shrine.webclient.shared.domain.Or;
import net.shrine.webclient.shared.domain.Term;

import org.junit.Test;

/**
 * 
 * @author clint
 * @date May 21, 2012
 */
public class QuerySummarizerTestGwt extends AbstractWebclientTest {

	@Test
	public void testSummarizeIterableOfReadOnlyQueryGroup() {
		try {
			QuerySummarizer.summarize(null);
			
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		try {
			QuerySummarizer.summarize(Collections.<ReadOnlyQueryGroup>emptyList());
			
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }

		final Term t1 = new Term("foo", "demographics", "foo");
		final Term t2 = new Term("bar", "diagnoses", "bar");
		final Term t3 = new Term("nuh", "diagnoses", "nuh");
		
		final Or or = new Or(t2, t3);
		
		final ReadOnlyQueryGroup qg1 = mockQueryGroup("X", t1);
		final ReadOnlyQueryGroup qg2 = mockQueryGroup("Y", or);
		
		final String expected1 = "who were <span class=\"querySummaryLabel0\">foo</span>";
		
		final String expected2 = "diagnosed with at least one of the diagnosis concepts in group <span class=\"querySummaryLabel1\">Y</span>";
		
		final String expected = "Find the number of patients per medical center " + expected1 + "<span class=\"joiner\">&nbsp;AND&nbsp;</span>" + expected2;
		
		assertEquals(expected, QuerySummarizer.summarize(asList(qg1, qg2)));
	}
	
	@Test
	public void testLabelsForCategories() {
		assertEquals("who were", toLabel("dEmoGRaPhiCs")); //should be case-insensitive
		assertEquals("diagnosed with", toLabel("dIaGnOsEs"));
		assertEquals("prescribed or administered", toLabel("MedIcaTioNS"));
		assertEquals("tested for levels of", toLabel("LAbs"));
		
		assertEquals("who were not", toNegatedLabel("dEmoGRaPhiCs")); //should be case-insensitive
		assertEquals("not diagnosed with", toNegatedLabel("dIaGnOsEs"));
		assertEquals("not prescribed or administered", toNegatedLabel("MedIcaTioNS"));
		assertEquals("not tested for levels of", toNegatedLabel("LAbs"));
		
		try {
			toLabel("asldjklasdj");
			
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		try {
			toLabel(null);
			
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		try {
			toNegatedLabel("asldjklasdj");
			
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
		
		try {
			toNegatedLabel(null);
			
			fail("Should have thrown");
		} catch(IllegalArgumentException expected) { }
	}
	
	@Test
	public void testSingularNamesForCategories() {
		assertEquals("demographic", toSingularCategory("dEmoGRaPhiCs"));
		assertEquals("diagnosis", toSingularCategory("dIaGnOsEs"));
		assertEquals("medication", toSingularCategory("MedIcaTioNS"));
		assertEquals("lab test", toSingularCategory("LAbs"));
	}
	
	@Test
	public void testSummarizeReadOnlyQueryGroup() {
		QuerySummarizer.summarize(mockQueryGroup("foo", new Term("foo", "demographics")), "someCssClass");
		
		QuerySummarizer.summarize(mockQueryGroup("foo", new Or(new Term("foo", "demographics"))), "someCssClass");
		
		try {
			QuerySummarizer.summarize(mockQueryGroup("foo", new And()), "someCssClass");
			
			fail("Should only accept Terms and Ors, not Ands");
		} catch(IllegalArgumentException expected) { }
		
		//1 term
		{
			final Term demoTerm = new Term("foo", "demographics", "foo");
			final Term diagTerm = new Term("foo", "diagnoses", "foo");
			final Term medicationTerm = new Term("foo", "medications", "foo");
			final Term labterm = new Term("foo", "labs", "foo");
			
			//NOT negated
			assertEquals("who were <span class=\"someClass\">foo</span>", QuerySummarizer.summarize(mockQueryGroup("X", demoTerm), "someClass"));
			assertEquals("diagnosed with <span class=\"someClass\">foo</span>", QuerySummarizer.summarize(mockQueryGroup("X", diagTerm), "someClass"));
			assertEquals("prescribed or administered <span class=\"someClass\">foo</span>", QuerySummarizer.summarize(mockQueryGroup("X", medicationTerm), "someClass"));
			assertEquals("tested for levels of <span class=\"someClass\">foo</span>", QuerySummarizer.summarize(mockQueryGroup("X", labterm), "someClass"));
			
			try {
				QuerySummarizer.summarize(mockQueryGroup("asdf", new Term("", "")), "someClass");
				
				fail("Should throw on unknown category");
			} catch(IllegalArgumentException expected) { }
			
			//negated
			assertEquals("who were not <span class=\"someClass\">foo</span>", QuerySummarizer.summarize(mockQueryGroup("X", demoTerm, true), "someClass"));
			assertEquals("not diagnosed with <span class=\"someClass\">foo</span>", QuerySummarizer.summarize(mockQueryGroup("X", diagTerm, true), "someClass"));
			assertEquals("not prescribed or administered <span class=\"someClass\">foo</span>", QuerySummarizer.summarize(mockQueryGroup("X", medicationTerm, true), "someClass"));
			assertEquals("not tested for levels of <span class=\"someClass\">foo</span>", QuerySummarizer.summarize(mockQueryGroup("X", labterm, true), "someClass"));
		}
		
		// more than 1 term
		{
			for(final String category : QuerySummarizer.Labels.forCategory.keySet()) {
				final Term t1 = new Term("foo", category, "foo");
				final Term t2 = new Term("nuh", category, "nuh");
				
				final Or or = new Or(t1, t2);
				
				final ReadOnlyQueryGroup queryGroup = mockQueryGroup("X", or);
				
				final String expected = toLabel(t1.getCategory()) + " at least one of the " + toSingularCategory(category) + " concepts in group <span class=\"someClass\">X</span>";
				
				assertEquals(expected, QuerySummarizer.summarize(queryGroup, "someClass"));
				
				final String expectedWhenNegated = toNegatedLabel(t1.getCategory()) + " any of the " + toSingularCategory(category) + " concepts in group <span class=\"someClass\">X</span>";
				
				final ReadOnlyQueryGroup negatedQueryGroup = mockQueryGroup("X", or, true);
				
				assertEquals(expectedWhenNegated, QuerySummarizer.summarize(negatedQueryGroup, "someClass"));
			}
		}
	}

	@Test
	public void testColor() {
		final String cssClass = "someClass";
		final String text = "blahblah";

		final String html = QuerySummarizer.color(text, cssClass);

		assertEquals("<span class=\"" + cssClass + "\">" + text + "</span>", html);
	}

	private static final ReadOnlyQueryGroup mockQueryGroup(final String name, final Expression expr) {
		return mockQueryGroup(name, expr, false);
	}
	
	private static final ReadOnlyQueryGroup mockQueryGroup(final String name, final Expression expr, final boolean negated) {
		return new ReadOnlyQueryGroup() {
			@Override
			public void observedBy(final Observer observer) { }

			@Override
			public void notifyObservers() { }

			@Override
			public void noLongerObservedBy(final Observer observer) { }

			@Override
			public String toXmlString() {
				return null;
			}

			@Override
			public boolean isNegated() {
				return negated;
			}

			@Override
			public Date getStart() {
				return null;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public int getMinOccurances() {
				return 0;
			}

			@Override
			public int getId() {
				return 0;
			}

			@Override
			public Expression getExpression() {
				return expr;
			}

			@Override
			public Date getEnd() {
				return null;
			}

			@Override
			public Date getCreatedOn() {
				return null;
			}
		};
	}
}
