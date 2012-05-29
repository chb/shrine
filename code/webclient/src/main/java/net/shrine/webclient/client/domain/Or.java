package net.shrine.webclient.client.domain;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.shrine.webclient.client.util.Util;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
public final class Or implements Andable, Iterable<Term>{

	private final List<Term> terms = Util.makeArrayList();

	public Or(final Collection<Term> terms) {
		super();

		Util.requireNotNull(terms);

		this.terms.addAll(terms);
	}

	public Or(final Term... terms) {
		this(asList(terms));
	}
	
	public int size() {
		return terms.size();
	}

	public boolean isEmpty() {
		return terms.isEmpty();
	}

	public Iterator<Term> iterator() {
		return terms.iterator();
	}

	@Override
	public Collection<Term> getTerms() {
		return Util.makeArrayList(terms);
	}

	public Or with(final Term term) {
		Util.requireNotNull(term);

		if (terms.contains(term)) {
			return this;
		}

		final List<Term> newTerms = Util.makeArrayList(terms);

		newTerms.add(term);

		return new Or(newTerms);
	}

	public Or without(final Term term) {
		Util.requireNotNull(term);

		if (!terms.contains(term)) {
			return this;
		}

		final List<Term> newTerms = Util.makeArrayList(terms);

		newTerms.remove(term);

		return new Or(newTerms);
	}

	@Override
	public String toXmlString() {
		if (terms.size() == 1) {
			return terms.get(0).toXmlString();
		}

		final List<String> xmls = Util.makeArrayList();

		for (final Term t : terms) {
			xmls.add(t.toXmlString());
		}

		return "<or>" + Util.join("", xmls) + "</or>";
	}

	@Override
	public String toString() {
		return "Or" + terms;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (terms == null ? 0 : terms.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Or other = (Or) obj;
		if (terms == null) {
			if (other.terms != null) {
				return false;
			}
		} else if (!terms.equals(other.terms)) {
			return false;
		}
		return true;
	}
}
