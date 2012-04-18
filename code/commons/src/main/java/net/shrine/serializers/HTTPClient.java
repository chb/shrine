package net.shrine.serializers;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.IOException;

/**
 * @author Bill Simons
 * @date Aug 3, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public class HTTPClient
{
    public static String post(String s, String url) throws IOException
    {
        PostMethod method = new PostMethod(url);
        RequestEntity entity = new StringRequestEntity(s, "text/xml", null);
        method.setRequestEntity(entity);
        HttpClient client = new HttpClient();
        client.executeMethod(method);
        return method.getResponseBodyAsString();
    }
}
