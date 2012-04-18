package net.shrine.webclient.client.domain;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 * 
 *       Stupid tag iface :(
 */
public interface Expression extends Serializable, XmlAble {
	public static final Expression Null = new Expression() {
		private static final long serialVersionUID = 1L;

		public String toXmlString() {
			//TODO
			return "<and/>";
		}

		@Override
		public Collection<Term> getTerms() {
			return Collections.emptyList();
		}
	};
	
	Collection<Term> getTerms();
}
