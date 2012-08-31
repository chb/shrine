package net.shrine.webclient.shared;

/**
 * 
 * @author clint
 * @date Aug 31, 2012
 */
public enum Cookies {
    
    Version("SHRINE-Version");
    
    public final String cookieName;
    
    private Cookies(final String cookieName) {
        this.cookieName = cookieName;
    }
}
