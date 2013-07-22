package net.shrine.serializers.hive;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * This class starts up an embedded http server that'll be our mock crc service.  It defers its handling to
 * a MockHttpHandler to be implemented by the constructor of this embedded server. 
 *
 *
 * @author Justin Quan
 * @date Apr 27, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public class MockHttpEmbeddedServer
{
    public final static String HOSTNAME = "localhost";
    public final static int PORT = 20201;
    public final static String RESOURCE = "/";

    private HttpServer embededServer;

    public MockHttpEmbeddedServer() throws IOException {
        this(new MockHttpEchoHandler());
    }

    public MockHttpEmbeddedServer(final MockHttpHandler handler) throws IOException {
        embededServer = HttpServer.create(new InetSocketAddress(MockHttpEmbeddedServer.HOSTNAME, MockHttpEmbeddedServer.PORT), 10);
        
        HttpHandler handlerWrapper = new HttpHandler() {
            public void handle(HttpExchange t) throws IOException {
                int contentLength = Integer.parseInt(t.getRequestHeaders().getFirst("Content-Length"));
                byte[] buffer = new byte[contentLength];
                InputStream is = t.getRequestBody();
                is.read(buffer);
                try {
                    String responseBody = handler.handle(new String(buffer));

                    t.sendResponseHeaders(200, responseBody.length());
                    OutputStream os = t.getResponseBody();
                    os.write(responseBody.getBytes());
                    os.close();

                } catch (MockHttpHandlerException e) {
                    throw new IOException(e);
                }
            }
        };
        embededServer.createContext(RESOURCE, handlerWrapper);
        embededServer.setExecutor(null); // creates a default executor
    }

    public static String getUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        sb.append(HOSTNAME);
        sb.append(":");
        sb.append(PORT);
        sb.append(RESOURCE);

        return sb.toString();
    }

    public void start() {
        embededServer.start();
    }

    public void shutdown() {
        embededServer.stop(3);  // wait 3 seconds
    }
}