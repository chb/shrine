package net.shrine.utilities.query;

import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import junit.framework.Assert;
import org.spin.tools.SPINUnitTest;
import org.spin.tools.config.ConfigTool;

import java.io.File;

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
 *         Date: Nov 9, 2010
 *         Time: 8:57:56 AM
 */
public class FlatFileIteratorTest extends SPINUnitTest
{
    public void testReadingMultiplePanels() throws Exception
    {
        File file = ConfigTool.getConfigFileFromClassloadedDir("net/shrine/utilities/query/FlatFileIterator.txt");

        FlatFileIterator iter = new FlatFileIterator(file);

        assertTrue(iter.hasNext());

        QueryDefinitionType maleOrDeceased = iter.next();

        Assert.assertEquals(1, maleOrDeceased.getPanel().size()); // Single panel
        Assert.assertEquals(2, maleOrDeceased.getPanel().get(0).getItem().size()); //Male, Female

        QueryDefinitionType maleAndDeceased = iter.next();

        Assert.assertEquals(2, maleAndDeceased.getPanel().size()); // Two Panels
        Assert.assertEquals(1, maleAndDeceased.getPanel().get(0).getItem().size()); //Male
        Assert.assertEquals(1, maleAndDeceased.getPanel().get(0).getItem().size()); //Deceased

    }
}
