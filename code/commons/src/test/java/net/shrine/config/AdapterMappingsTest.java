package net.shrine.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;

import org.spin.tools.FileUtils;
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

    public void skip_testGetInstanceReturnsTheSameObject() throws Exception
    {
        final AdapterMappings one = AdapterMappings.getDefaultInstance();

        final AdapterMappings two = AdapterMappings.getDefaultInstance();

        assertSame("getDefaultInstance should always return the same object", one, two);
    }

    public void testLoadFromFile() throws Exception
    {
        // write to a temp file for direct file reading
        final File tempfile = File.createTempFile("AdapterMappings", ".xml");
        final String contents = FileUtils.read(ShrineConfigTestResources.AdapterMappings_DEM_AGE_0_9.getInputStream());
        FileUtils.write(tempfile, contents);
        final AdapterMappings mappings = AdapterMappings.loadfromFile(tempfile);

        // Count the number of <entry>'s in the file to determine how many
        // global entries we should expect to have. NOTE: expects that the
        // adaptermapppings xml file is pretty-printed, looks for <entry>
        // alone on one line.
        int expectedEntries = 0;
        final String entryTag = "<entry>";
        final BufferedReader in = new BufferedReader(new InputStreamReader(ShrineConfigTestResources.AdapterMappings_DEM_AGE_0_9.getInputStream()));

        for(String line = in.readLine(); line != null; line = in.readLine())
        {
            if(line.trim().equalsIgnoreCase(entryTag))
            {
                expectedEntries++;
            }
        }

        assertEquals("Should have loaded " + expectedEntries + " mappings", expectedEntries, mappings.size());

        final String expectedHostname = "localhost-test";
        assertEquals("Hostname should be " + expectedHostname, expectedHostname, mappings.getHostname());

        final int expectedTimestampYear = 1970;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(mappings.getTimestamp());

        assertEquals("Timestamp should be " + expectedTimestampYear, expectedTimestampYear, cal.get(Calendar.YEAR));
    }

    public void testApplyI2B2Prefix() throws Exception
    {
        final String no_prefix = "\\TEST\\PATH";
        final String has_prefix = AdapterMappings.I2B2_PREFIX + no_prefix;

        assertEquals("Should apply prefix where there is none", has_prefix, AdapterMappings.applyI2B2Prefix(no_prefix));
        assertEquals("Should apply prefix when the local path begins with \\I2B2(single slash)", "\\\\I2B2\\I2B2\\TEST\\PATH", AdapterMappings.applyI2B2Prefix("\\I2B2" + no_prefix));
        assertEquals("Should not apply the prefix when it is already there.", has_prefix, AdapterMappings.applyI2B2Prefix(has_prefix));

    }

    public void testGetMappings() throws Exception
    {
        final AdapterMappings mappings = AdapterMappings.loadfromStream(ShrineConfigTestResources.AdapterMappings_DEM_AGE_0_9.getInputStream());

        assertNotNull(mappings.getMappings(CORE_KEY_INVALID));
        assertSame(0, mappings.getMappings(CORE_KEY_INVALID).size());

        assertNotNull(mappings.getMappings(CORE_KEY_DEMOGRAPHICS_0_9));
        assertSame(10, mappings.getMappings(CORE_KEY_DEMOGRAPHICS_0_9).size());

        try
        {
            final List<String> local_keys = mappings.getMappings(CORE_KEY_DEMOGRAPHICS_0_9);
            local_keys.add("Better hope not");
            fail("Should not be able to add directly");
        }
        catch(final UnsupportedOperationException uoe)
        {
            // Good.
        }
    }

    public void testAddMapping() throws Exception
    {
        final AdapterMappings mappings = AdapterMappings.loadfromStream(ShrineConfigTestResources.AdapterMappings_DEM_AGE_0_9.getInputStream());

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
