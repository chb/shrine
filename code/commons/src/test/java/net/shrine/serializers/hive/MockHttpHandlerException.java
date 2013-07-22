package net.shrine.serializers.hive;

/**
 * @author Andrew McMurry, MS
 *         <p/>
 *         With primary support from Children's Hospital Informatics Program @
 *         Harvard-MIT Health Sciences and Technology and
 *         <p/>
 *         Secondary support from the Harvard Medical School
 *         Center for BioMedical Informatics
 *         <p/>
 *         PHD candidate, Boston University Bioinformatics
 *         Member, I2b2 National Center for Biomedical Computing
 *         <p/>
 *         All works licensed under LGPL
 *         <p/>
 *         User: andy
 *         Date: May 14, 2010
 *         Time: 2:17:18 PM
 */

public class MockHttpHandlerException extends Exception
{
    public MockHttpHandlerException()
    {
        super();
    }

    public MockHttpHandlerException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public MockHttpHandlerException(final String message)
    {
        super(message);
    }

    public MockHttpHandlerException(final Throwable cause)
    {
        super(cause);
    }

}
