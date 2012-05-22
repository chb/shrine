package net.shrine.webclient.client.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.shrine.webclient.client.domain.Expression;
import net.shrine.webclient.client.domain.Or;
import net.shrine.webclient.client.domain.ReadOnlyQueryGroup;
import net.shrine.webclient.client.domain.Term;

import com.allen_sauer.gwt.log.client.Log;

/**
 * 
 * @author clint
 * @date May 3, 2012
 */
public final class QuerySummarizer {

	private QuerySummarizer() {
		super();
	}

	public static String summarize(final Iterable<? extends ReadOnlyQueryGroup> queryGroups) {
		Log.info("Summarizing queries: " + queryGroups);
		
		Util.requireNotNull(queryGroups);
		Util.require(queryGroups.iterator().hasNext());

		final StringBuilder result = new StringBuilder();

		result.append("Find the number of patients per medical center ");

		final List<String> querySummaries = Util.makeArrayList();

		{
			int i = 0;
		
			for (final ReadOnlyQueryGroup query : queryGroups) {
				final String queryGroupCssClass = "querySummaryLabel" + (i % Util.numRowColors);
				
				querySummaries.add(summarize(query, queryGroupCssClass));
				
				++i;
			}
		}

		result.append(Util.join("<span class=\"joiner\">&nbsp;AND&nbsp;</span>", querySummaries));

		return result.toString();
	}
	
	static final String summarize(final ReadOnlyQueryGroup queryGroup, final String queryGroupCssClass) {
		final StringBuilder result = new StringBuilder();
		
		final Expression expr = queryGroup.getExpression();
		
		Util.require(expr instanceof Term || expr instanceof Or);
		
		if(expr instanceof Term) {
			final Term term = (Term)expr;
			
			final String category = queryGroup.isNegated() ? toNegatedLabel(term.getCategory()) : toLabel(term.getCategory());
			
			Util.requireNotNull(category);
			
			result.append(category).append(" ").append(color(term.getSimpleName(), queryGroupCssClass));
		} else if(expr instanceof Or) {
			final Or or = (Or)expr;
			
			final List<Term> terms = Util.toList(or.getTerms());
			
			//FIXME (Perhaps REVISITME): Allow multi-category expressions; will make the summary less accurate,
			//but multi-category expressions are rare (says Andy), and will be avoided at the 
			// 8 May 2012 demo.
			//requireAllSameCategory(terms);
			
			final String firstCategory = terms.get(0).getCategory();
			
			final String categoryLabel = queryGroup.isNegated() ? toNegatedLabel(firstCategory) : toLabel(firstCategory);
			
			Util.requireNotNull(categoryLabel);
			
			final String categoryName = toSingularCategory(firstCategory);
			
			if(categoryName == null) {
				Log.warn("couldn't find singluar category for '" + firstCategory + "'");
			}
			
			Util.requireNotNull(categoryName);
			
			final String quantifier = queryGroup.isNegated() ? "any" : "at least one";
			
			result.append(categoryLabel).append(" ").append(quantifier).append(" of the ").append(categoryName).append(" concepts in group ").append(color(queryGroup.getName(), queryGroupCssClass));
		}

		return result.toString();
	}
	
	static String color(final String text, final String queryGroupCssClass) {
		return "<span class=\"" + queryGroupCssClass + "\">" + text + "</span>";
	}
	
	/*static void requireAllSameCategory(final Collection<Term> terms) {
		if(terms.size() == 0) {
			return;
		}
		
		if(terms.size() == 1) {
			return;
		}
		
		final String firstCategory = terms.iterator().next().getCategory();
		
		for(final Term term : terms) {
			Util.require(firstCategory.equals(term.getCategory()));
		}
	}*/
	
	static final String toLabel(final String category) {
		Util.requireNotNull(category);
		
		final String mungedCategory = category.toLowerCase();
		
		requireIsKnownCategory(mungedCategory);
		
		return Labels.forCategory.get(mungedCategory).value;
	}
	
	static final String toNegatedLabel(final String category) {
		Util.requireNotNull(category);
		
		final String mungedCategory = category.toLowerCase();
		
		requireIsKnownCategory(mungedCategory);
		
		return Labels.forCategory.get(mungedCategory).negated;
	}

	private static void requireIsKnownCategory(final String category) {
		Util.require(Labels.forCategory.containsKey(category));
	}
	
	static final String toSingularCategory(final String category) {
		return QuerySummarizer.Labels.singularCategories.get(category.toLowerCase());
	}
	
	static final class Labels {
		private Labels() {
			super();
		}

		@SuppressWarnings("serial")
		static final Map<String, Label> forCategory = new HashMap<String, Label>() {{
			this.put("demographics", new Label("who were", "who were not"));
			this.put("diagnoses", new Label("diagnosed with"));
			this.put("medications", new Label("prescribed or administered"));
			this.put("labs", new Label("tested for levels of"));
		}};
		
		@SuppressWarnings("serial")
		static final Map<String, String> singularCategories = new HashMap<String, String>() {{
			this.put("demographics", "demographic");
			this.put("diagnoses", "diagnosis");
			this.put("medications", "medication");
			this.put("labs", "lab test");
		}};

		static final String defaultCategoryLabel = "with a record of";
		
		static final class Label {
			public final String value;
			public final String negated;

			private Label(final String value) {
				this(value, null);
			}
			
			private Label(final String value, final String negated) {
				super();
				
				this.value = value;
				
				this.negated = (negated != null) ? negated : "not " + value;
			}
		}
	}
}
