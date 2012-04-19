package net.shrine.webclient.client.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import net.shrine.webclient.client.util.Util;

/**
 * 
 * @author clint
 * @date Mar 28, 2012
 * 
 * TODO: All this manual XML building feels very bad
 */
public final class ExpressionXml {
	private ExpressionXml() {
		super();
	}
	
	public static String fromQueryGroups(final Collection<QueryGroup> queryGroups) {
		Util.require(queryGroups.size() > 0);
		
		final List<String> exprs = Util.makeArrayList();
		
		for(final QueryGroup group : queryGroups) {
			exprs.add(group.toXmlString());
		}
		
		if(exprs.size() == 1) {
			return exprs.get(0);
		} else {
			return "<and>" + Util.join(exprs) + "</and>";
		}
	}
}
