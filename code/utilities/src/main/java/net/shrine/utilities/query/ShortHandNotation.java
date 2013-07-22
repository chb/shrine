package net.shrine.utilities.query;

/**
 * @author amcmurry
*/
public enum ShortHandNotation
{
    /**
     * New Query Definition
     */
    def,

    /**
     * Query alias name
     * TODO not yet supported
     */
    name,

    /**
     * 1..* panels per query
     */
    panel,

    /**
     * 1...* items per panel
     */
    item,

    /**
     * End of File
     */
    end;

    public String print()
    {
        return "@"+name();
    }

    public static boolean isNotation(String string)
    {
        return string.contains("@");
    }

    public static ShortHandNotation parse(String string)
    {
        return ShortHandNotation.valueOf(string.replaceFirst("@", ""));
    }

}
