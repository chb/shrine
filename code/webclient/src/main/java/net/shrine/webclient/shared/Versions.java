package net.shrine.webclient.shared;




/**
 * 
 * @author clint
 * @date Aug 31, 2012
 */
public final class Versions {
    
    public static final Versions Unknown = new Versions("unknown", "unknown", "unknown", "unknown"); 
    
    public final String appVersion;

    public final String revision;

    public final String branch;

    public final String buildDate;

    public Versions(final String appVersion, final String revision, final String branch, final String buildDate) {
        super();
        
        this.appVersion = appVersion;
        this.revision = revision;
        this.branch = branch;
        this.buildDate = buildDate;
    }

    //Pick something that's unlikely to be used as an HTTP or regex delimiter; doesn't need to be pretty.
    private static final String delim = "%%%";
    
    @Override
    public String toString() {
        return appVersion + delim + revision + delim + branch + delim + buildDate;
    }
    
    private static String stripQuotes(final String s) {
        final String withoutFirstQuote = s.replaceFirst("\"", "");
        
        if(withoutFirstQuote.endsWith("\"")) {
            return withoutFirstQuote.substring(0, withoutFirstQuote.length() - 1);
        }
        
        return withoutFirstQuote;
    }
    
    public static Versions fromString(final String serialized) {
        //TODO: Why does the cookie value come wrapped in quotes?
        final String[] parts = stripQuotes(serialized).split(delim);
        
        return new Versions(parts[0], parts[1], parts[2], parts[3]);
    }
}
