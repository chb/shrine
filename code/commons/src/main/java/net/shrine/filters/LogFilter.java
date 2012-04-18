package net.shrine.filters;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import javax.servlet.*;
import java.io.IOException;
import java.util.Random;

/**
 * @author Justin Quan
 * @date Nov 30, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public class LogFilter implements Filter {
    private static final Logger log = Logger.getLogger(LogFilter.class);
    public static final String GRID = "globalRequestId";
    public static final String LRID = "localRequestId";
    private static final String IS_GLOBAL_HEAD = "isGlobalHead";

    private final Random rand = new Random();
    private boolean isGlobalHead;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String stringGlobal = filterConfig.getInitParameter(IS_GLOBAL_HEAD);
        isGlobalHead = stringGlobal != null && Boolean.parseBoolean(stringGlobal);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if(isGlobalHead) {
            MDC.put(GRID, Long.toHexString(rand.nextLong()));
        } else {
            // TODO: when we start to pass along the globalRequestId along w/ subsequent requests, read it out here
            // Unfortunately shrine sends messages out via SPIN which doesn't give us a facility yet to pass in our own tracking ids, yet
        }
        // Disabled due to questionable usefulness
//        MDC.put(LRID, Long.toHexString(rand.nextLong()));

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
