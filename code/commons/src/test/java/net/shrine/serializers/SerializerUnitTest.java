package net.shrine.serializers;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.spin.tools.JAXBUtils;
import org.spin.tools.SPINUnitTest;
import org.w3c.dom.Node;

import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;

public abstract class SerializerUnitTest extends SPINUnitTest 
{
	private DifferenceListener differenceListener = new I2B2XMLDifferenceListener();
	
	protected DifferenceListener getDifferenceListener()
	{
		//allow subclasses to override, not that anyone ever will.
		return differenceListener;
	}

	public <T> void roundTripRequest(I2B2ExampleMessages message) throws Exception
    {
        roundTrip(message, RequestMessageType.class);
    }

//    public <T> void roundTripRequest(String filename) throws Exception
//    {
//        roundTrip(filename, RequestMessageType.class);
//    }

    public <T> void roundTripResponse(I2B2ExampleMessages message) throws Exception
    {
        roundTrip(message, ResponseMessageType.class);
    }

//    public <T> void roundTripResponse(String filename) throws Exception
//    {
//        roundTrip(filename, ResponseMessageType.class);
//    }

    public <T> void roundTrip(I2B2ExampleMessages message, java.lang.Class<T> clazz ) throws Exception
    {
        String XML = message.getXML();

        log.debug("original"+ XML);

        T type = JAXBUtils.unmarshal(XML, clazz);

        String roundTripped = JAXBUtils.marshalToString(type);

        log.debug(roundTripped);

        // assertEquals(XML.trim(), roundTripped.trim());
        assertSimilarXML(XML, roundTripped);
    }

	public void assertSimilarXML(String original, String test) throws Exception
	{
		assertSimilarXML(null, original, test);
	}
	
	public void assertDissimilarXML(String original, String test) throws Exception
	{
		assertDissimilarXML(null, original, test);
	}
	
	private void assertDissimilarXML(String message, String original, String test) throws Exception
	{
		compareXML(message, original, test, false);
	}

	private void compareXML(String message, String original, String test, boolean shouldBeSimilar) throws Exception
	{
	    Diff diff = new Diff(original, test);
	    diff.overrideDifferenceListener(getDifferenceListener());
	    if (diff.similar() != shouldBeSimilar) {
	        fail(getFailMessage(message, diff));
	    }
	}

	public void assertSimilarXML(String message, String original, String test) throws Exception
	{
		compareXML(message, original, test, true);
	}

	protected String getFailMessage(String msg, Diff diff)
	{
        StringBuffer sb = new StringBuffer();
        if (msg != null && msg.length() > 0) {
            sb.append(msg).append(", ");
        }
        return sb.append(diff.toString()).toString();
	}
	
	/*
	 * The XMLUnit API allows us to use our own DIfferenceListener to determine
	 * whether a difference is significant or not. Here we use it to insulate
	 * against minor diffs between the I2B2 and SHRINE serialization of
	 * request/responce messages
	 */
	class I2B2XMLDifferenceListener implements DifferenceListener
	{

		public int differenceFound(Difference difference)
		{
			if (difference.getId() == DifferenceConstants.NAMESPACE_PREFIX_ID)
			{
				// Two nodes use different prefixes for the same XML Namespace URI in the two pieces of XML.
				return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
			}
			else if ((difference.getId() == DifferenceConstants.TEXT_VALUE_ID) && 
					(difference.getControlNodeDetail().getValue().trim()
						.equals(difference.getTestNodeDetail().getValue().trim())))
			{    
				// The value of two texts is different in the two pieces of XML.
				// ..but ignore leading/trailing whitespace differences
				return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
			}
			else 
			{
				// it's (maybe) a real difference
				log.warn("Found XML difference:"
						+ String.valueOf(difference.getId()) + " - " + String.valueOf(difference) 
						+ "\n\t" + difference.getControlNodeDetail().getXpathLocation() 
						+ "\n\t" + difference.getTestNodeDetail().getXpathLocation());
				return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;				
			}
		}
		public void skippedComparison(Node control, Node test){/*no-op*/}
	}
	
}
