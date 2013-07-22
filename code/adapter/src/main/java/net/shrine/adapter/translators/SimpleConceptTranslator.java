package net.shrine.adapter.translators;

import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import net.shrine.adapter.AdapterMappingException;
import net.shrine.config.AdapterMappings;
import net.shrine.serializers.ShrineJAXBUtils;
import org.spin.query.message.serializer.SerializationException;
import org.spin.tools.config.ConfigException;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author David Ortiz
 * @Date June 28, 2010
 * <p/>
 * This class is similiar
 */
public final class SimpleConceptTranslator extends DefaultConceptTranslator
{
    public SimpleConceptTranslator() throws ConfigException
    {
        super();
    }

    public SimpleConceptTranslator(final AdapterMappings mappings)
    {
        super(mappings);
    }

    public SimpleConceptTranslator(final Map<String, List<String>> adaptorMappings)
    {
        super(adaptorMappings);
    }

    @Override
    protected void translatePanel(final PanelType panel) throws SerializationException, AdapterMappingException
    {
        final List<ItemType> items = panel.getItem();
        final List<ItemType> translatedItems = new ArrayList<ItemType>();

        for(final ItemType item : items)
        {
            final List<String> locals = mappings.getMappings(item.getItemKey());
            if(locals.isEmpty())
            {
                // No mapping found, just continue on and hope it's ok,
                // hopefully
                // somebody knows what they're doing
                translatedItems.add(item);

            }
            for(final String local : locals)
            {
                try
                {
                    ItemType translatedItem = ShrineJAXBUtils.copy(item);
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
        items.clear();
        items.addAll(translatedItems);
    }
}
