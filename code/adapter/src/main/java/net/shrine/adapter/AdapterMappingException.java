package net.shrine.adapter;

/**
 * REFACTORED
 * 
 * @author Andrew McMurry, MS
 * @date Jan 6, 2010 (REFACTORED 1.6.6)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */

public class AdapterMappingException extends AdapterException
{
    private static final long serialVersionUID = 1L;

    public AdapterMappingException()
    {
        super();
    }

    public AdapterMappingException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public AdapterMappingException(final String message)
    {
        super(message);
    }

    public AdapterMappingException(final Throwable cause)
    {
        super(cause);
    }
}