package net.shrine.serializers.hive;

import edu.harvard.i2b2.crc.datavo.i2b2message.*;
import edu.harvard.i2b2.crc.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.crc.datavo.pm.ObjectFactory;
import org.apache.log4j.Logger;
import org.spin.query.message.serializer.SerializationException;
import org.spin.tools.JAXBUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.GregorianCalendar;

/**
 * REFACTORED
 *
 * @author Andrew McMurry, MS
 * @date Jan 6, 2010 (REFACTORED)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public abstract class HiveCommonSerializer
{
    public static final Logger log = Logger.getLogger(HiveCommonSerializer.class);

    public static final boolean DEBUG = log.isDebugEnabled();
    public static final boolean INFO = log.isInfoEnabled();

    public static final String STATUS_ERROR = "ERROR";
    public static final String STATUS_DONE = "DONE";

    public static final void addResponseHeaderWithDoneStatus(final ResponseMessageType response)
    {
        response.setResponseHeader(new ResponseHeaderType(getResultStatusDone("DONE")));
    }
    
    public static RequestMessageType getRequest(final File xml) throws IOException, JAXBException
    {
        final Object o = JAXBUtils.unmarshal(xml, HiveJaxbContext.getInstance().getPackageList());
        return ((JAXBElement<RequestMessageType>) o).getValue();
    }

    public static RequestMessageType getRequest(final String xml) throws JAXBException
    {
        final Object o = JAXBUtils.unmarshal(xml, HiveJaxbContext.getInstance().getPackageList());
        return ((JAXBElement<RequestMessageType>) o).getValue();
    }

    public static ResponseMessageType getResponse(final File xml) throws IOException, JAXBException
    {
        final Object o = JAXBUtils.unmarshal(xml, HiveJaxbContext.getInstance().getPackageList());
        return ((JAXBElement<ResponseMessageType>) o).getValue();
    }

    public static ResponseMessageType getResponse(final String xml) throws JAXBException
    {
        final Object o = JAXBUtils.unmarshal(new StringReader(xml), HiveJaxbContext.getInstance().getPackageList());
        return ((JAXBElement<ResponseMessageType>) o).getValue();
    }

    public static String toXMLString(final RequestMessageType requestMessageType) throws JAXBException
    {
        final Marshaller m = HiveJaxbContext.getInstance().getContext().createMarshaller();
        final StringWriter sw = new StringWriter();
        m.marshal(requestMessageType, sw);
        return sw.toString();
    }

    public static String toXMLString(final ResponseMessageType responseMessageType) throws JAXBException
    {
        final Marshaller m = HiveJaxbContext.getInstance().getContext().createMarshaller();
        final StringWriter sw = new StringWriter();
        m.marshal(responseMessageType, sw);
        return sw.toString();
    }

    public static RequestMessageType getTemplateRequestMessageType(final SecurityType securityType, final String projectId)
    {
        final RequestMessageType request = getTemplateRequestMessageType();

        request.getMessageHeader().setSecurity(securityType);
        request.getMessageHeader().setProjectId(projectId);

        return request;
    }

    /**
     * Returns a response message type using a request message as the template.
     * NOTE: the response message header is NOT set to any default using this
     * method.
     *
     * @param requestMessageType
     * @return
     */
    public static ResponseMessageType getTemplateResponseMessageType(final RequestMessageType requestMessageType)
    {
        final ResponseMessageType response = new ResponseMessageType();

        response.setMessageHeader(requestMessageType.getMessageHeader());
        response.setMessageBody(new BodyType());

        return response;
    }

    /**
     * Synthesize a new error message using the request as the template.
     *
     * @param requestMessageType
     * @param errorMessage
     * @return ResponseMessageType that includes your supplied error message.
     */
    public static ResponseMessageType getTemplateResponseMessageTypeError(final RequestMessageType requestMessageType, final String errorMessage)
    {
        final ResponseMessageType response = getTemplateResponseMessageType(requestMessageType);
        //
        response.setResponseHeader(getResponseHeaderError(errorMessage));
        //
        return response;
    }

    protected static String getDefaultSendingApplicationVersion()
    {
        return "1.3-compatible"; // 1.6.8.1?
    }

    protected static String getDefaultSendingApplicationName()
    {
        return "SHRINE";
    }

    protected static String getDefaultSendingFacilityName()
    {
        return "SHRINE";
    }

    public static String getPMGetAllUserParamsRequestString(final SecurityType securityType) throws JAXBException
    {
        RequestMessageType request = getTemplateRequestMessageType(securityType, "");
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<String> getAllUserParam = objectFactory.createGetAllUserParam("<username>" + securityType.getUsername() + "</username>");
        request.getMessageBody().getAny().add(getAllUserParam);
        return toXMLString(request);
    }

    public static String getPMUserAuthRequestString(final SecurityType securityType) throws JAXBException
    {
        RequestMessageType request = getTemplateRequestMessageType(securityType, "");
        GetUserConfigurationType getUserConfiguration = new GetUserConfigurationType();
        getUserConfiguration.getProject().add("undefined");
        request.getMessageBody().getAny().add(getUserConfiguration);
        return toXMLString(request);
    }

    public static SecurityType getSecurityType(final RequestMessageType requestMessageType)
    {
        return requestMessageType.getMessageHeader().getSecurity();
    }

    public static SecurityType getSecurityType(final ResponseMessageType responseMessageType)
    {
        return responseMessageType.getMessageHeader().getSecurity();
    }

    public static void setSecurityType(final RequestMessageType requestMessageType, final SecurityType credentials)
    {
        requestMessageType.getMessageHeader().setSecurity(credentials);
    }

    public static void setSecurityType(final ResponseMessageType responseMessageType, final SecurityType credentials)
    {
        responseMessageType.getMessageHeader().setSecurity(credentials);
    }

    public static Object getRequestBodyNodeObject(final RequestMessageType request, final int index)
    {
        // Sometimes the objects in the Any collection are ElementNSImpl's and
        // sometimes they are typed
        final Object o = request.getMessageBody().getAny().get(index);
        return o;
    }

    public static Object getResponseBodyNodeObject(final ResponseMessageType response, final int index) throws SerializationException
    {
        if(response.getMessageBody().getAny().size() == 0)
        {
            throw new SerializationException("Response contains no message body");
        }
        if(response.getMessageBody().getAny().size() < index)
        {
            throw new SerializationException("Response body size is smaller than requested");
        }

        return response.getMessageBody().getAny().get(index);
    }

    public static <T> T getBodyNode(final ResponseMessageType response, final int index, final Class<T> clazz) throws SerializationException
    {
        final Object o = getResponseBodyNodeObject(response, index);
        return tryToCast(o, clazz);
    }

    public static <T> T getBodyNode(final RequestMessageType request, final int index, final Class<T> clazz) throws SerializationException
    {
        final Object o = getRequestBodyNodeObject(request, index);
        return tryToCast(o, clazz);
    }

    /**
     * The objects in an Any() collection might be an
     * org.apache.xerces.dom.ElementNSImpl's, JAXBElement, or typed objects.
     * Check to se if they are already what we are looking for, or someting that
     * we can use (Node)
     *
     * @throws SerializationException
     */
    @SuppressWarnings("unchecked")
    private static <T> T tryToCast(final Object o, final Class<T> clazz) throws SerializationException
    {
        try
        {
            if(Node.class.isAssignableFrom(o.getClass()))
            {
                return JAXBUtils.unmarshal((Node) o, clazz);
            }
            else if(JAXBElement.class.isAssignableFrom(o.getClass()))
            {
                final JAXBElement element = (JAXBElement) o;
                return clazz.cast(element.getValue());
            }
            else
            {
                return clazz.cast(o);
            }
        }
        catch(final Exception e)
        {
            throw new SerializationException("Conversion Failed between " + getClassNameEvenIfWrapped(o) + " and " + String.valueOf(clazz), e);
        }
    }

    private static final String getClassNameEvenIfWrapped(final Object o)
    {
        if(o == null)
        {
            return null;
        }

        if(JAXBElement.class.isAssignableFrom(o.getClass()))
        {
            final JAXBElement<?> element = (JAXBElement<?>) o;

            return element.getValue().getClass().getName();
        }

        return o.getClass().getName();
    }

    public static <T> T getBodyNodeSingle(final ResponseMessageType response, final Class<T> clazz) throws SerializationException
    {
        return getBodyNode(response, 0, clazz);
    }

    public static <T> T getBodyNodeSingle(final RequestMessageType request, final Class<T> clazz) throws SerializationException
    {
        return getBodyNode(request, 0, clazz);
    }

    public static void clearBodyNode(final RequestMessageType request) throws SerializationException
    {
        clearBodyNode(request.getMessageBody());
    }

    public static void clearBodyNode(final ResponseMessageType response) throws SerializationException
    {
        clearBodyNode(response.getMessageBody());
    }

    public static void clearBodyNode(final BodyType body) throws SerializationException
    {
        body.getAny().clear();
    }

    @Deprecated
    public static <T> void addBodyNode(final ResponseMessageType response, final T bodyNode) throws SerializationException
    {
        addBodyNode(response, response.getMessageBody().getAny().size(), bodyNode);
    }

    @Deprecated
    public static <T> void addBodyNode(final ResponseMessageType response, final int index, final T bodyNode) throws SerializationException
    {
        try
        {
            final Element element = JAXBUtils.marshalToElement(bodyNode);
            response.getMessageBody().getAny().add(index, element);
        }
        catch(final Exception e)
        {
            throw new SerializationException("could not add query result instance", e);
        }
    }

    @Deprecated
    public static <T> void updateBodyNode(final ResponseMessageType response, final int index, final T bodyNode) throws SerializationException
    {
        response.getMessageBody().getAny().remove(index);
        response.getMessageBody().getAny().add(index, bodyNode);
    }

    @Deprecated
    public static <T> void addBodyNode(final RequestMessageType request, final T bodyNode) throws SerializationException
    {
        addBodyNode(request, request.getMessageBody().getAny().size(), bodyNode);
    }

    @Deprecated
    public static <T> void addBodyNode(final RequestMessageType request, final int index, final T bodyNode) throws SerializationException
    {
        try
        {
            final Element element = JAXBUtils.marshalToElement(bodyNode);
            request.getMessageBody().getAny().add(index, element);
        }
        catch(final Exception e)
        {
            throw new SerializationException("could not add query result instance", e);
        }
    }

    @Deprecated
    public static <T> void updateBodyNode(final RequestMessageType request, final int index, final T bodyNode) throws SerializationException
    {
        try
        {
            final Element bodyElement = JAXBUtils.marshalToElement(bodyNode);

            request.getMessageBody().getAny().remove(index);
            request.getMessageBody().getAny().add(index, bodyElement);
        }
        catch(final Exception e)
        {
            throw new SerializationException("Failed to update body node", e);
        }
    }

    public static <T> void add(final ResponseMessageType response, final Class<T> clazz) throws SerializationException
    {
        try
        {
            final Element element = JAXBUtils.marshalToElement(clazz);
            response.getMessageBody().getAny().add(element);
        }
        catch(final Exception e)
        {
            throw new SerializationException("could not add query result instance", e);
        }
    }

    public static void stripLocalUserPassword(final RequestMessageType request)
    {
        if(DEBUG)
        {
            log.debug("Stripping local user credentials before broadcasting the message.");
        }

        request.getMessageHeader().getSecurity().setPassword(null);
    }

    // ---

    public static RequestMessageType getTemplateRequestMessageType()
    {
        final RequestMessageType requestMessageType = new RequestMessageType();

        requestMessageType.setMessageHeader(getTemplateMessageHeaderType());
        requestMessageType.setRequestHeader(getTemplateRequestHeaderType());
        requestMessageType.setMessageBody(new BodyType());

        return requestMessageType;
    }

    public static MessageHeaderType getTemplateMessageHeaderType()
    {
        final MessageHeaderType messageHeaderType = new MessageHeaderType();
        messageHeaderType.setSecurity(new SecurityType());

        messageHeaderType.setI2B2VersionCompatible(Constants.i2b2_version_compatible);
        messageHeaderType.setHl7VersionCompatible(Constants.hl7_version_compatible);

        // So far unecessary to work with public i2b2 service.
        // messageHeaderType.getSendingApplication().setApplicationName("i2b2 Ontology");
        // messageHeaderType.getSendingApplication().setApplicationVersion("1.3");
        // messageHeaderType.getSendingFacility().setFacilityName("i2b2 Hive");

        final ApplicationType appType = new ApplicationType();
        appType.setApplicationName(getDefaultSendingApplicationName());
        appType.setApplicationVersion(getDefaultSendingApplicationVersion());
        messageHeaderType.setSendingApplication(appType);
        final FacilityType facilityType = new FacilityType();
        facilityType.setFacilityName(getDefaultSendingFacilityName());
        messageHeaderType.setSendingFacility(facilityType);

        // So far unecessary to work with public i2b2 service.
        // messageHeaderType.getReceivingApplication().setApplicationName("Ontology Cell");
        // messageHeaderType.getReceivingApplication().setApplicationVersion("1.3");
        // messageHeaderType.getReceivingFacility().setFacilityName("i2b2 Hive");

        try
        {
            // So far unecessary to work with public i2b2 service, but
            // timestamps are nice to have
            final XMLGregorianCalendar xmlNow = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
            messageHeaderType.setDatetimeOfMessage(xmlNow);
        }
        catch(final DatatypeConfigurationException e)
        {
            log.error("Failed to generate Message Timestamp", e);
        }

        return messageHeaderType;
    }

    public static RequestHeaderType getTemplateRequestHeaderType()
    {
        final RequestHeaderType requestHeaderType = new RequestHeaderType();
        // TODO: defaults here

        // So far unecessary to work with public i2b2 service.
        // requestHeaderType.setResultWaittimeMs(120000);
        return requestHeaderType;
    }

    public static ResponseHeaderType getResponseHeaderDone()
    {
        return new ResponseHeaderType(getResultStatusDone(null));
    }

    public static ResponseHeaderType getResponseHeaderPending(final String messageText)
    {
        return new ResponseHeaderType(getResultStatusPending(messageText));
    }

    public static ResponseHeaderType getResponseHeaderError(final String messageText)
    {
        return new ResponseHeaderType(getResultStatusError(messageText));
    }

    public static final String DONE = "DONE";
    public static final String ERROR = "ERROR";
    public static final String PENDING = "PENDING";

    public static ResultStatusType getResultStatusDone(final String messageText)
    {
        return new ResultStatusType(new StatusType(DONE, messageText));
    }

    public static ResultStatusType getResultStatusError(final String messageText)
    {
        return new ResultStatusType(new StatusType(ERROR, messageText));
    }

    public static ResultStatusType getResultStatusPending(final String messageText)
    {
        return new ResultStatusType(new StatusType(PENDING, messageText));
    }

    public static StatusType getResponseStatusType(final ResponseMessageType response)
    {
        try
        {
            return response.getResponseHeader().getResultStatus().getStatus();
        }
        catch(final Exception e)
        {
            log.error("getResponseStatusType Failed!", e);
        }
        return null;
    }
}
