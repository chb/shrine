package net.shrine.utilities.query;

import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import net.shrine.config.AdapterMappings;
import net.shrine.serializers.crc.QueryDefBuilder;
import org.apache.log4j.Logger;
import org.spin.tools.config.ConfigException;

import java.util.Iterator;

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
 *         Time: 11:03:51 AM
 */
public class AdapterMappingsIterator extends QueryDefIterator
{
    public static final Logger log     = Logger.getLogger(AdapterMappingsIterator.class);

    AdapterMappings mappings;
    boolean         translate;

    private Iterator<String> iterator;

    public AdapterMappingsIterator(String adapterMappingsFile, boolean translate) throws ConfigException
    {
        log.info("AdapterMappingsIterator init, translate = "+translate + "  file="+adapterMappingsFile );
        
        this.mappings               = AdapterMappings.loadFromFile(adapterMappingsFile);
        this.translate              = translate;
        this.iterator               = mappings.getEntries().iterator();
    }

    @Override
    public boolean hasNext()
    {
        return iterator.hasNext();
    }

    //TODO: implement randomization 

    @Override
    public QueryDefinitionType next()
    {
        String global = iterator.next();

        if(translate)
        {
            ItemType[] items = QueryDefBuilder.getItems(mappings.getMappings(global)) ;

            return QueryDefBuilder.getQueryDefinition(items);
        }
        else
        {
            return QueryDefBuilder.getQueryDefinition( global );
        }
    }
}

