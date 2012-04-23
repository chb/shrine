package net.shrine.webclient.client.domain;

import java.io.Serializable;
import java.util.Collection;

/**
 * 
 * @author clint
 * @date Mar 23, 2012
 * 
 *       Stupid tag iface :(
 */
public interface Expression extends Serializable, XmlAble {
	//TODO: this has a bad smell; not all expressions are collections of terms,
	//and it's not necessarily right to collapse subexpressions into one big list
	//of terms.
	Collection<Term> getTerms();
}
