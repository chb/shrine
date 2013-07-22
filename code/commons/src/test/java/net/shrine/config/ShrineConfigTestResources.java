package net.shrine.config;

import java.io.IOException;
import java.io.InputStream;

import org.spin.tools.FileUtils;
import org.spin.tools.config.ConfigException;
import org.spin.tools.config.ConfigTool;

/**
 * REFACTORED
 * 
 * @author Andrew McMurry, MS
 * @date Feb 22, 2010 (REFACTORED)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */

public enum ShrineConfigTestResources
{
    AdapterMappings_DEM_AGE_0_9;

    public String getFilename()
    {
        return name() + ".xml";
    }

    public InputStream getInputStream() throws ConfigException
    {
        return ConfigTool.getConfigFileStream(getFilename());
    }

    public String readFromStream() throws ConfigException, IOException
    {
        return FileUtils.read(getInputStream());
    }
}
