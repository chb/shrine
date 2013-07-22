package net.shrine.adapter.translators;

import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import net.shrine.adapter.AdapterMappingException;
import net.shrine.config.AdapterMappings;
import net.shrine.serializers.ShrineJAXBUtils;
import org.apache.log4j.Logger;
import org.spin.query.message.serializer.SerializationException;
import org.spin.tools.config.ConfigException;

import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.Map;

import static org.spin.tools.Util.makeArrayList;

/**
 * @Author David Ortiz
 * @Date June 28, 2010
 * <p/>
 * This implementation of Translator is particularly intolerant of missing
 * mappings in the Adaptor. It is meant for use in the Harvard deployment
 * where each concept should be mapped and an unmapped concept is an error
 * condition and we should fail as noisily as possible.
 * <p/>
 * Other shrine deployments such as CARRAnet where mapping is less import,
 * the simple translator is more tolerant of errors while still allowing
 * for some mapping to take place.
 * @see SimpleConceptTranslator
 */
public class DefaultConceptTranslator
{
    public static final Logger log = Logger.getLogger(DefaultConceptTranslator.class);
    public static final boolean DEBUG = log.isDebugEnabled();

    protected final AdapterMappings mappings;

    public DefaultConceptTranslator() throws ConfigException
    {
        mappings = AdapterMappings.getDefaultInstance();
    }

    public DefaultConceptTranslator(final AdapterMappings mappings)
    {
        this.mappings = mappings;
    }

    public DefaultConceptTranslator(final Map<String, List<String>> adaptorMappings)
    {
        mappings = new AdapterMappings();

        for(final String s : adaptorMappings.keySet())
        {
            for(final String s2 : adaptorMappings.get(s))
            {
                mappings.addMapping(s, s2);
            }
        }
    }

    public void translateQueryDefinition(final QueryDefinitionType queryDef) throws SerializationException, AdapterMappingException
    {
        final List<PanelType> panels = queryDef.getPanel();

        for(final PanelType panel : panels)
        {
            translatePanel(panel);
        }
    }

    protected void translatePanel(final PanelType panel) throws SerializationException, AdapterMappingException
    {
        final List<ItemType> items = panel.getItem();
        final List<ItemType> translatedItems = makeArrayList();

        for(final ItemType item : items)
        {
            final List<String> locals = mappings.getMappings(item.getItemKey());

            for(final String local : locals)
            {
                try
                {
                    final ItemType translatedItem = ShrineJAXBUtils.copy(item);
                    translatedItem.setItemKey(local);
                    translatedItems.add(translatedItem);
                }
                catch(final JAXBException jaxbe)
                {
                    final String msg = "Translation error- failed to copy construct :" + String.valueOf(item);

                    log.error(msg, jaxbe);

                    throw new SerializationException(msg, jaxbe);
                }
            }
        }

        if(translatedItems.isEmpty())
        {
            throw new AdapterMappingException(String.format("Panel %d contains no mappable terms", panel.getPanelNumber()));
        }

        items.clear();
        items.addAll(translatedItems);
    }
}
