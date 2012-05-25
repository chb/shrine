package net.shrine.webclient.client.util;

import java.util.Iterator;

/**
 * 
 * @author clint
 * @date Mar 26, 2012
 * @param <T>
 */
public interface ReadOnlyObservable<T> extends IObservable, Iterable<T> {
	T get();
	
	boolean isDefined();
    
    boolean isEmpty();
    
    T getOrElse(final T defaultValue);
    
    Iterator<T> iterator();
}
