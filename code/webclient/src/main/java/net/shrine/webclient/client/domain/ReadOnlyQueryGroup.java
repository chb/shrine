package net.shrine.webclient.client.domain;

import java.util.Date;

import net.shrine.webclient.client.QueryGroupId;
import net.shrine.webclient.client.util.IObservable;

/**
 * 
 * @author clint
 * @date Apr 4, 2012
 */
public interface ReadOnlyQueryGroup extends IObservable {

	public abstract String toXmlString();

	public abstract Expression getExpression();

	public abstract QueryGroupId getId();

	public abstract int getMinOccurances();

	public abstract boolean isNegated();

	public abstract Date getStart();

	public abstract Date getEnd();
	
	public abstract Date getCreatedOn();
}