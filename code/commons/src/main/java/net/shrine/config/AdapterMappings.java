package net.shrine.config;

import org.apache.log4j.Logger;
import org.spin.tools.FileUtils;
import org.spin.tools.JAXBUtils;
import org.spin.tools.config.ConfigException;
import org.spin.tools.config.ConfigTool;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * AdapterMappings specify "global/core" shrine concepts to local key item keys.
 * A single global concept can have MANY local item key mappings. The
 * AdapterMappings files are not intended to be edited by hand, since they
 * contain literally thousands of terms. The AdapterMappings files are created
 * by --> Extracting SHRIMP output, --> Transforming the item paths by calling
 * the Ontology cell to obtain hierarchical path information --> Loading them
 * into this output file bound by JAXB
 *
 * @author Andrew McMurry, MS
 * @date Jan 6, 2010 (REFACTORED)
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 * <p/>
 * REFACTORED from 1.6.6
 * @see AdapterMappings
 */

@XmlRootElement(name = "AdapterMappings")
@XmlAccessorType(XmlAccessType.FIELD)
public class AdapterMappings {
    private static final Logger log = Logger.getLogger(AdapterMappings.class);
    private static final boolean DEBUG = log.isDebugEnabled();
    private static final boolean INFO = log.isInfoEnabled();

    private static final String DEFAULT_MAPPINGS_FILENAME = "AdapterMappings.xml";
    public static final String I2B2_PREFIX = "\\\\I2B2";

    private static AdapterMappings defaultInstance;

    private String hostname;
    private Date timestamp;
    private final TreeMap<String, LocalKeys> mappings = new TreeMap<String, LocalKeys>();

    public AdapterMappings() {
        hostname = ConfigTool.getHostName();
        timestamp = new Date();
    }

    public static AdapterMappings getDefaultInstance() throws ConfigException {
        if (defaultInstance != null) {
            if (DEBUG) {
                log.debug("getDefaultInstance() returning cached instance");
            }
        } else {
            defaultInstance = loadFromFile(DEFAULT_MAPPINGS_FILENAME);
        }

        return defaultInstance;
    }

    public static AdapterMappings loadFromFile(String mappingsFilename)
            throws ConfigException {
        return loadfromFile(ConfigTool
                .getConfigFileWithFailover(mappingsFilename));
    }

    public String getHostname() {
        return hostname;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public List<String> getMappings()
    {
        return Collections.unmodifiableList(new ArrayList(mappings.keySet()));
    }

    public List<String> getMappings(String globalKey) {
        LocalKeys keys = mappings.get(globalKey);
        if (keys != null) {
            return Collections.unmodifiableList(keys);
        } else {
            return Collections.unmodifiableList(new ArrayList<String>(0));
        }
    }

    public static AdapterMappings loadfromStream(InputStream inputStream) throws ConfigException {
        AdapterMappings mappings = null;
        try {
            if (INFO) {
                log.info("Loading AdapterMappings from stream : " + String.valueOf(inputStream));
            }

            mappings = JAXBUtils.unmarshal(FileUtils.read(inputStream), AdapterMappings.class);
            return mappings;
        }
        catch (IOException ioe) {
            log.error("IO Error", ioe);
            throw new ConfigException("AdapterMappings io error", ioe);
        }
        catch (JAXBException jaxbe) {
            log.error("Unmarshalling error", jaxbe);
            throw new ConfigException("AdapterMappings unmarshalling error", jaxbe);
        }
    }

    public static AdapterMappings loadfromFile(File mappingsFile) throws ConfigException {
        AdapterMappings mappings = null;
        try {
            if (INFO) {
                log.info("Loading AdapterMappings from disk : "
                        + mappingsFile.getAbsolutePath());
            }
            mappings = JAXBUtils.unmarshal(mappingsFile, AdapterMappings.class);
            return mappings;
        }
        catch (JAXBException jaxbe) {
            log.error("Unmarshalling error", jaxbe);
            throw new ConfigException("AdapterMappings unmarshalling error",
                    jaxbe);
        }
    }

    public int size() {
        return mappings.size();
    }

    public static String applyI2B2Prefix(String key) {
        if (!key.startsWith(I2B2_PREFIX)) {
            String new_key = I2B2_PREFIX + key;
            log.debug("Adding i2b2 prefix for local_key:" + key + "->"
                    + new_key);
            return new_key;
        } else {
            return key;
        }
    }

    public boolean addMapping(String core_key, String local_key) {
        if (mappings.containsKey(core_key)) {
            // TODO if there is a uniqueness constraint on local_key mappings,
            // then this should be a Set, not a List
            List<String> keys = mappings.get(core_key);
            if (keys.contains(local_key)) {
                return false;
            } else {
                return keys.add(local_key);
            }
        } else {
            LocalKeys keys = new LocalKeys(local_key);
            mappings.put(core_key, keys);
            return true;
        }
    }

    public Set<String> getEntries() {
        return Collections.unmodifiableSet(mappings.keySet());

    }
}
