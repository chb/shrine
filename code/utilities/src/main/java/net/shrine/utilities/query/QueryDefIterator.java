package net.shrine.utilities.query;

import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import org.apache.log4j.Logger;
import org.spin.tools.Util;

import java.util.Iterator;
import java.util.List;

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
 *         Date: Oct 19, 2010
 *         Time: 1:11:25 PM
 */
public abstract class QueryDefIterator implements Iterator<QueryDefinitionType>
{
    public static final Logger log  = Logger.getLogger(QueryDefIterator.class);

    public List<QueryDefinitionType> all()
    {
        List<QueryDefinitionType> all = Util.makeArrayList();

        while(hasNext()) all.add(next());

        return all;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("Nothing to remove.");
    }
}
