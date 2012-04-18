package net.shrine.config;

import java.util.List;

import org.spin.tools.JAXBUtils;
import org.spin.tools.SPINUnitTest;

/**
 * @author Andrew McMurry, MS
 * @date Jan 6, 2010 (REFACTORED)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public final class AdapterMappingsTest extends SPINUnitTest
{
    private static final String CORE_KEY_DEMOGRAPHICS_0_9 = "\\\\i2b2\\i2b2\\Demographics\\Age\\0-9 years old\\";
    private static final String CORE_KEY_TEST = "\\\\i2b2\\i2b2\\TEST\\KEY\\";
    private static final String CORE_KEY_INVALID = "THIS IS NOT A VALID GLOBAL KEY";
    private static final String LOCAL_KEY_DEMOGRAPHICS_AGE_4 = "\\\\i2b2\\LOCAL\\DEM|AGE:4";
    private static final String LOCAL_KEY_DEMOGRAPHICS_AGE_TEST = "\\\\i2b2\\LOCAL\\DEM|AGE:TEST";

    public void testGetMappings() throws Exception
    {
    	final AdapterMappings mappings = (new ClasspathAdapterMappingsSource("AdapterMappings_DEM_AGE_0_9.xml")).load();

        assertNotNull(mappings.getMappings(CORE_KEY_INVALID));
        assertSame(0, mappings.getMappings(CORE_KEY_INVALID).size());

        assertNotNull(mappings.getMappings(CORE_KEY_DEMOGRAPHICS_0_9));
        assertSame(10, mappings.getMappings(CORE_KEY_DEMOGRAPHICS_0_9).size());

        final List<String> localKeys = mappings.getMappings(CORE_KEY_DEMOGRAPHICS_0_9);
        
        try
        {
            localKeys.add("Better hope not");
            
            fail("Should not be able to add directly");
        }
        catch(final UnsupportedOperationException expected) { }
    }

    public void testAddMapping() throws Exception
    {
    	final AdapterMappings mappings = (new ClasspathAdapterMappingsSource("AdapterMappings_DEM_AGE_0_9.xml")).load();

        assertFalse("Should not add duplicate local_keys", mappings.addMapping(CORE_KEY_DEMOGRAPHICS_0_9, LOCAL_KEY_DEMOGRAPHICS_AGE_4));
        assertSame(10, mappings.getMappings(CORE_KEY_DEMOGRAPHICS_0_9).size());

        assertTrue("Should add a new local_key succesfully", mappings.addMapping(CORE_KEY_DEMOGRAPHICS_0_9, LOCAL_KEY_DEMOGRAPHICS_AGE_TEST));
        assertSame(11, mappings.getMappings(CORE_KEY_DEMOGRAPHICS_0_9).size());

        assertSame(0, mappings.getMappings(CORE_KEY_TEST).size());
        assertTrue("Should add a new core+local key conbo successully", mappings.addMapping(CORE_KEY_TEST, LOCAL_KEY_DEMOGRAPHICS_AGE_TEST));
        assertSame(1, mappings.getMappings(CORE_KEY_TEST).size());

    }

    public void testSerialize() throws Exception
    {
        final AdapterMappings m = new AdapterMappings();

        m.addMapping("core1", "local1");
        m.addMapping("core1", "local2");

        m.addMapping("core2", "local1");
        m.addMapping("core2", "local2");
        m.addMapping("core2", "local3");

        final String xml = JAXBUtils.marshalToString(m);
        final AdapterMappings m1 = JAXBUtils.unmarshal(xml, AdapterMappings.class);
        final String xml1 = JAXBUtils.marshalToString(m1);

        assertEquals(xml, xml1);
    }
}
