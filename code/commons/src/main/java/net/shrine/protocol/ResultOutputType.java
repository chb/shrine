package net.shrine.protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bill Simons
 * @date 8/30/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
//NB: This MUST be a Java enum, or else the Groovy compiler will blow up when building the utilities module.
public enum ResultOutputType {
    PATIENTSET(false),
    PATIENT_COUNT_XML(false), 
    PATIENT_AGE_COUNT_XML(true),
    PATIENT_RACE_COUNT_XML(true),
    PATIENT_VITALSTATUS_COUNT_XML(true),
    PATIENT_GENDER_COUNT_XML(true),
    ERROR(false);
    
    public final boolean isBreakdown;

    private ResultOutputType(final boolean isBreakdown) {
        this.isBreakdown = isBreakdown;
    }
    
    public static ResultOutputType[] breakdownTypes() {
        final List<ResultOutputType> results = new ArrayList<ResultOutputType>();
        
        for(final ResultOutputType resultType : values()) {
            if(resultType.isBreakdown) {
                results.add(resultType);
            }
        }
        
        return results.toArray(new ResultOutputType[0]);
    }
    
    public static ResultOutputType[] nonBreakdownTypes() {
        final List<ResultOutputType> results = new ArrayList<ResultOutputType>();
        
        for(final ResultOutputType resultType : values()) {
            if(!resultType.isBreakdown) {
                results.add(resultType);
            }
        }
        
        return results.toArray(new ResultOutputType[0]);
    }
}
