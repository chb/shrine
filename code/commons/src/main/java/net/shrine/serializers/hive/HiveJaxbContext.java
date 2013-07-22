package net.shrine.serializers.hive;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * This is a singleton class that wraps a JAXBContext that has been initialized contain understanding
 * of all i2b2 messages as well as shrine messages.
 * <p/>
 * Both this class and jaxbcontexts are thread safe.
 * <p/>
 * TODO: this is probably inefficient as the consumer is likely to create a new marshaller
 * from this context on every invocation.  We should better support packages in JAXBUtils, and then this
 * can all go away.
 *
 * @author Justin Quan
 * @author Mutaambabyona W. Maasha
 * @date Jun 7, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public final class HiveJaxbContext
{
    private static HiveJaxbContext instance;

    private final JAXBContext context;

    private final String packageList;

    private HiveJaxbContext() throws JAXBException
    {
        final String[] packages = {
                "edu.harvard.i2b2.crc.datavo.i2b2message",
                "edu.harvard.i2b2.crc.datavo.i2b2result",
                "edu.harvard.i2b2.crc.datavo.ontology",
                "edu.harvard.i2b2.crc.datavo.pdo",
                "edu.harvard.i2b2.crc.datavo.pdo.query",
                "edu.harvard.i2b2.crc.datavo.pm",
                "edu.harvard.i2b2.crc.datavo.setfinder.query"
        };

        final StringBuilder packageString = new StringBuilder();
        
        for(final String s : packages)
        {
            packageString.append(s).append(":");
        }
        
        packageList = packageString.toString();
        
        context = JAXBContext.newInstance(packageList);
    }

    public static synchronized HiveJaxbContext getInstance() throws JAXBException
    {
        if(instance == null)
        {
            instance = new HiveJaxbContext();
        }
        
        return instance;
    }

    public JAXBContext getContext()
    {
        return context;
    }

    public String getPackageList()
    {
        return packageList;
    }
}

