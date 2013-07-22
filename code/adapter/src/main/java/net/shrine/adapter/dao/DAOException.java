package net.shrine.adapter.dao;

/**
 * 
 * @author ??
 * 
 * Center for Biomedical Informatics (CBMI)
 * @link https://cbmi.med.harvard.edu/
 *
 */
public class DAOException extends Exception
{
    private static final long serialVersionUID = 1L;

    public DAOException()
    {
    }

    public DAOException(final String message)
    {
        super(message);
    }

    public DAOException(final Throwable cause)
    {
        super(cause);
    }

    public DAOException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
