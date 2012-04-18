package net.shrine.webclient.client.domain;

import java.util.Date;
import java.util.HashMap;

import net.shrine.webclient.client.util.Observable;

/**
 * 
 * @author clint
 * @date Apr 4, 2012
 */
public interface ReadOnlyQueryGroup {

	public abstract String toXmlString();

	public abstract Expression getExpression();

	public abstract Observable<HashMap<String, IntWrapper>> getResult();

	public abstract int getMinOccurances();

	public abstract boolean isNegated();

	public abstract Date getStart();

	public abstract Date getEnd();

}