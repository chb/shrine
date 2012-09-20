package net.shrine.webclient.client.widgets;

import java.util.Arrays;

import net.shrine.webclient.client.AbstractWebclientTest;

/**
 * 
 * @author clint
 * @date Sep 20, 2012
 */
public class ChartPanelTestGwt extends AbstractWebclientTest {
    public void testBreakdownTypeToBreakdownNameMapping() {
        assertEquals("", ChartPanel.getBreakdownName(""));
        assertEquals("", ChartPanel.getBreakdownName(null));
        assertEquals("", ChartPanel.getBreakdownName("ajksfhjksafh"));
        
        assertEquals("Age", ChartPanel.getBreakdownName("PATIENT_AGE_COUNT_XML"));  
        assertEquals("Race", ChartPanel.getBreakdownName("PATIENT_RACE_COUNT_XML"));
        assertEquals("Vital Status", ChartPanel.getBreakdownName("PATIENT_VITALSTATUS_COUNT_XML"));
        assertEquals("Gender", ChartPanel.getBreakdownName("PATIENT_GENDER_COUNT_XML"));
    }
    
    public void testBreakdownNameIsUsed() {
        for(final String breakdownType : Arrays.asList("", null, "kasljdlasd"))
        {
            doChartNameTest(breakdownType, "");
        }
        
        doChartNameTest("PATIENT_AGE_COUNT_XML", "Age");
        doChartNameTest("PATIENT_RACE_COUNT_XML", "Race");
        doChartNameTest("PATIENT_VITALSTATUS_COUNT_XML", "Vital Status");
        doChartNameTest("PATIENT_GENDER_COUNT_XML", "Gender");
    }
    
    private static void doChartNameTest(final String breakdownType, final String expectedName) {
        final ChartPanel chartPanel = new ChartPanel(breakdownType, null);
        
        assertEquals(expectedName, chartPanel.chartNameSpan.getInnerText());
    }
}
