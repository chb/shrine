package net.shrine.serializers.pm;

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

public class PMGeneralException extends Exception 
{
	private static final long serialVersionUID = 1L;

	public PMGeneralException()
    {
        super();
    }

    public PMGeneralException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public PMGeneralException(final String message)
    {
        super(message);
    }

    public PMGeneralException(final Throwable cause)
    {
        super(cause);
    }
}