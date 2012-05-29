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

	String toXmlString();

	Expression getExpression();

	int getId();

	String getName();
	
	int getMinOccurances();

	boolean isNegated();

	Date getStart();

	Date getEnd();
	
	Date getCreatedOn();
}