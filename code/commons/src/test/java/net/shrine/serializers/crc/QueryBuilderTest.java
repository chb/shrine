package net.shrine.serializers.crc;

import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import junit.framework.Assert;
import org.spin.tools.SPINUnitTest;

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
 *         Date: Oct 12, 2010
 *         Time: 1:11:48 PM
 */
public class QueryBuilderTest extends SPINUnitTest {
    public void testSinglePanelWithGenderMaleORFemale() throws Exception {
        QueryDefinitionType queryDef = QueryDefBuilder.getQueryDefinition(QueryDefBuilder.getItemGenderFemale(), QueryDefBuilder.getItemGenderMale());
        assertTrue("Expect a single panel ", queryDef.getPanel().size() == 1);
        PanelType singlePanel = queryDef.getPanel().get(0);

        assertTrue("Expect 2 items keys ", singlePanel.getItem().size() == 2);

        ItemType item1 = singlePanel.getItem().get(0);
        ItemType item2 = singlePanel.getItem().get(1);

        assertEquals("Female ", QueryDefBuilder.PATH_FEMALE, item1.getItemKey());
        assertEquals("Male ", QueryDefBuilder.PATH_MALE, item2.getItemKey());
    }

    public void testDualPanelGenderMaleAndFemale() throws Exception {
        PanelType panelFemale = QueryDefBuilder.getPanel(QueryDefBuilder.getItemGenderFemale());
        PanelType panelMale = QueryDefBuilder.getPanel(QueryDefBuilder.getItemGenderMale());
        QueryDefinitionType queryDef = QueryDefBuilder.getQueryDefinition(panelFemale, panelMale);
        Assert.assertEquals("Expect 2 panels ", 2, queryDef.getPanel().size());

        Assert.assertEquals("Each panel has only one item ", 1, queryDef.getPanel().get(0).getItem().size());
        Assert.assertEquals("Each panel has only one item ", 1, queryDef.getPanel().get(1).getItem().size());

        assertEquals("This is the Female panel", QueryDefBuilder.PATH_FEMALE, queryDef.getPanel().get(0).getItem().get(0).getItemKey());

        assertEquals("This is the Male panel",QueryDefBuilder.PATH_MALE, queryDef.getPanel().get(1).getItem().get(0).getItemKey());
    }

    public void testBuildPaths() {
        String pathNoPrefix = QueryDefBuilder.buildPath("Diagnoses", "Complications of pregnancy, childbirth, and the puerperium");
        String pathWithPrefix = QueryDefBuilder.buildPathWithPrefix("SHRINE", "Diagnoses", "Complications of pregnancy, childbirth, and the puerperium");

        assertEquals("Diagnoses\\Complications of pregnancy, childbirth, and the puerperium\\", pathNoPrefix);
        assertEquals("\\\\SHRINE\\Diagnoses\\Complications of pregnancy, childbirth, and the puerperium\\", pathWithPrefix);
    }
}
