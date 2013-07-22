package net.shrine.adapter.translators;

import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import net.shrine.adapter.AdapterMappingException;
import org.junit.Test;
import org.spin.query.message.serializer.SerializationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author Bill Simons
 * @date 2/15/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public class DefaultConceptTranslatorTest
{

    @Test
    public void testTranslatePanel() throws SerializationException, AdapterMappingException
    {
        Map<String, List<String>> mappings = new HashMap<String, List<String>>();
        mappings.put("concept1", Arrays.asList("local1a", "local1b", "local1c"));

        DefaultConceptTranslator translator = new DefaultConceptTranslator(mappings);

        PanelType panel = createPanel("concept1", "NOCONCEPT");
        translator.translatePanel(panel);

        assertEquals(3, panel.getItem().size());

        Collection<String> actualConcepts = new ArrayList<String>();
        for(ItemType type : panel.getItem())
        {
            actualConcepts.add(type.getItemKey());
        }

        assertTrue(actualConcepts.containsAll(Arrays.asList("local1a", "local1b", "local1c")));
    }

    @Test(expected = AdapterMappingException.class)
    public void testTranslatePanelWithNoMappedConcepts() throws SerializationException, AdapterMappingException
    {
        Map<String, List<String>> mappings = new HashMap<String, List<String>>();
        mappings.put("concept1", Arrays.asList("local1a", "local1b", "local1c"));

        DefaultConceptTranslator translator = new DefaultConceptTranslator(mappings);

        PanelType panel = createPanel("NOT_MAPPED", "NOCONCEPT");
        translator.translatePanel(panel);

    }

    private PanelType createPanel(String... itemKeys)
    {
        PanelType panel = new PanelType();
        for(String itemKey : itemKeys)
        {
            ItemType itemType = new ItemType();
            itemType.setItemKey(itemKey);
            panel.getItem().add(itemType);
        }
        return panel;
    }
}
