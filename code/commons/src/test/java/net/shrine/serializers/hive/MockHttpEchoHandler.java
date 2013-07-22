package net.shrine.serializers.hive;

/**
 * @author Justin Quan
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
 *         Date: May 14, 2010
 *         Time: 2:03:01 PM
 */
public class MockHttpEchoHandler implements MockHttpHandler
{    
    public String handle(String httpBody)
    {
        return httpBody;
    }
}
