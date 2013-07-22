package net.shrine.proxy;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.spin.extension.JDOMTool;
import org.spin.tools.config.ConfigException;
import org.spin.tools.PKITool;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * ----------------------------------------------------------
 * [ All net.shrine.* code is available per the I2B2 license]
 * @link https://www.i2b2.org/software/i2b2_license.html
 * ----------------------------------------------------------
 */
public class ShrineProxyServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(ShrineProxyServlet.class);
    private ShrineProxy proxy;

    @Override
    public void init(final ServletConfig config) throws ServletException
    {
        super.init(config);

       
        try
        {
       	      log.info("Starting ProxyServlet");

              PKITool.getInstance();

			  proxy = new ShrineProxy();
		}
        catch (ConfigException e)
        {
			log.error("ProxyServlet error: " + e);
			throw new ServletException(e);
		}
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        doPost(request, response);
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/xml");
        OutputStreamWriter out = new OutputStreamWriter(response.getOutputStream());

        try
        {
        	JDOMTool messageJDOM = new JDOMTool(request.getReader());
        	
        	String resp       = proxy.redirect(messageJDOM);
        	out.write(resp);
        	out.flush();
        	out.close();
        	
        	// Just forward the request
		} catch (Exception e) {

			log.error("ProxyServlet error:", e);
			throw new ServletException(e);
		}
    }
}

