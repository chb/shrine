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
	Collection<Term> getTerms();
}
