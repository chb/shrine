package net.shrine.serializers.hive;

/**
 * REFACTORED
 *
 * @author Andrew McMurry, MS
 * @date Jan 6, 2010 (REFACTORED 1.6.6)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */

public class HiveServiceFaultException extends Exception 
{
	private static final long serialVersionUID = 1L;

	public HiveServiceFaultException()
    {
        super();
    }

    public HiveServiceFaultException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public HiveServiceFaultException(final String message)
    {
        super(message);
    }

    public HiveServiceFaultException(final Throwable cause)
    {
        super(cause);
    }
}