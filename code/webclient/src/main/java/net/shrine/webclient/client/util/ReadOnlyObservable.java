package net.shrine.webclient.client.util;

import java.util.Iterator;

/**
 * 
 * @author clint
 * @date Mar 26, 2012
 * @param <T>
 */
public interface ReadOnlyObservable<T> extends IObservable, Iterable<T> {
	public T get();
	
	public boolean isDefined();
    
    public boolean isEmpty();
    
    public T getOrElse(final T defaultValue);
    
    public Iterator<T> iterator();
}
