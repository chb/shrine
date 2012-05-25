package net.shrine.webclient.client.state;

import java.util.Date;

import net.shrine.webclient.client.domain.Expression;
import net.shrine.webclient.client.util.IObservable;

/**
 * 
 * @author clint
 * @date Apr 4, 2012
 */
public interface ReadOnlyQueryGroup extends IObservable {

	public abstract String toXmlString();

	public abstract Expression getExpression();

	public abstract int getId();

	public abstract String getName();
	
	public abstract int getMinOccurances();

	public abstract boolean isNegated();

	public abstract Date getStart();

	public abstract Date getEnd();
	
	public abstract Date getCreatedOn();
}