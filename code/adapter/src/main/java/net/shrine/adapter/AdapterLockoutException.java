package net.shrine.adapter;

import org.spin.tools.crypto.signature.Identity;

/**
 * REFACTORED
 * 
 * @author Andrew McMurry, MS
 * @date Jan 6, 2010 (REFACTORED 1.6.6)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */

public class AdapterLockoutException extends AdapterException
{
    private static final long serialVersionUID = 1L;
    private final Identity identity;

    public AdapterLockoutException(final Identity identity)
    {
        this.identity = identity;
    }

    @Override
    public String getMessage()
    {
        return "AdapterLockoutException[" + "domain=" + identity.getDomain() + ", " + "username=" + identity.getUsername() + ", " + "]";
    }
}
