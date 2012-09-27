package net.shrine.webclient.client.widgets.suggest;

/**
 * 
 * @author clint
 * @date Sep 26, 2012
 */
public final class SequenceNumbers {
    private static int current = 0;

    private SequenceNumbers() {
        super();
    }
    
    public static int next() {
        return current++;
    }
}
