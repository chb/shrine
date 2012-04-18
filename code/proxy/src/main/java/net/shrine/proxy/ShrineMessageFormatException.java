package net.shrine.proxy;

/**
 * @author Andrew McMurry
 *
 * ----------------------------------------------------------
 * [ All net.shrine.* code is available per the I2B2 license]
 * @link https://www.i2b2.org/software/i2b2_license.html
 * ----------------------------------------------------------
 */
public class ShrineMessageFormatException extends Exception
{
    public ShrineMessageFormatException()
    {
        super();
    }

    public ShrineMessageFormatException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public ShrineMessageFormatException(final String message)
    {
        super(message);
    }

    public ShrineMessageFormatException(final Throwable cause)
    {
        super(cause);
    }
}
