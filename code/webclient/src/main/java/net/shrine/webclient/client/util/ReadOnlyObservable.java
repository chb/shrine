package net.shrine.webclient.client.util;

/**
 * 
 * @author clint
 * @date Mar 26, 2012
 * @param <T>
 */
public interface ReadOnlyObservable<T> extends IObservable {
	public T get();
	
	public boolean isDefined();
    
    public boolean isEmpty();
    
    public T getOrElse(final T defaultValue);
}
