package net.shrine.webclient.client.domain;

import java.util.Collection;
import java.util.Collections;

import net.shrine.webclient.client.util.Util;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 */
public final class Term implements Andable {
	private static final long serialVersionUID = 1L;
	
	public final String value;

	public Term(final String value) {
		super();

		Util.requireNotNull(value);

		this.value = value;
	}
	
	@Override
	public Collection<Term> getTerms() {
		return Collections.singletonList(this);
	}
	
	@Override
	public String toString() {
		return "Term(" + value + ")";
	}
	
	@Override
	public String toXmlString() {
		return "<term>" + value + "</term>";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (value == null ? 0 : value.hashCode());
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
		final Term other = (Term) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

}
