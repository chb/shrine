package net.shrine.webclient.client.domain;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.List;

import net.shrine.webclient.client.util.Util;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
public final class Or implements Andable {
	
	private static final long serialVersionUID = 1L;
	
	private final List<Term> terms = Util.makeArrayList();

	public Or(final Collection<Term> terms) {
		super();

		Util.requireNotNull(terms);

		this.terms.addAll(terms);
	}

	public Or(final Term... terms) {
		this(asList(terms));
	}

	@Override
	public Collection<Term> getTerms() {
		return Util.makeArrayList(terms);
	}

	public Or with(final Term term) {
		Util.requireNotNull(term);

		if(terms.contains(term)) {
			return this;
		}
		
		final List<Term> newTerms = Util.makeArrayList(terms);
		
		newTerms.add(term);
		
		return new Or(newTerms);
	}
	
	public Or without(final Term term) {
		Util.requireNotNull(term);

		if(!terms.contains(term)) {
			return this;
		}
		
		final List<Term> newTerms = Util.makeArrayList(terms);
		
		newTerms.remove(term);
		
		return new Or(newTerms);
	}

	@Override
	public String toXmlString() {
		if(terms.size() == 1) {
			return terms.get(0).toXmlString();
		}
		
		final List<String> xmls = Util.makeArrayList();
		
		for(final Term t : terms) {
			xmls.add(t.toXmlString());
		}
		
		return "<or>" + Util.join("", xmls) + "</or>";
	}
}
