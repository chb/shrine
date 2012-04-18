package net.shrine.authorization;

/**
 * @author Bill Simons
 * @date Aug 24, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public class AuthorizationException extends RuntimeException
{
    public AuthorizationException()
    {
        super();
    }

    public AuthorizationException(String message)
    {
        super(message);
    }

    public AuthorizationException(Throwable e)
    {
        super(e);
    }
}
