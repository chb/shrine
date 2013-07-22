package net.shrine.serializers;

import net.shrine.serializers.hive.HiveJaxbContext;
import org.spin.tools.JAXBUtils;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

/**
 * @author Bill Simons
 * @date Oct 14, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public class ShrineJAXBUtils
{

    public static final <T> T copy(T toBeCopied) throws JAXBException
    {
        Marshaller marshaller = HiveJaxbContext.getInstance().getContext().createMarshaller();
        StringWriter sw = new StringWriter();
        marshaller.marshal(toBeCopied, sw);
        Object o = JAXBUtils.unmarshal(sw.toString(), HiveJaxbContext.getInstance().getPackageList());
        if(o instanceof JAXBElement)
        {
            return ((JAXBElement<T>) o).getValue();
        }
        else
        {
            return (T) o;
        }
    }

}
