package net.shrine.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

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
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 *       <p/>
 *       REFACTORED from 1.6.6
 * @see AdapterMappings
 */
@XmlRootElement(name = "AdapterMappings")
@XmlAccessorType(XmlAccessType.FIELD)
public final class AdapterMappings {

	private final TreeMap<String, LocalKeys> mappings = new TreeMap<String, LocalKeys>();

	public AdapterMappings() {
		super();
	}

	public List<String> getMappings() {
		return Collections.unmodifiableList(new ArrayList<String>(mappings.keySet()));
	}

	public List<String> getMappings(final String globalKey) {
		final LocalKeys keys = mappings.get(globalKey);

		if (keys != null) {
			return Collections.unmodifiableList(keys);
		} else {
			return Collections.emptyList();
		}
	}

	public int size() {
		return mappings.size();
	}

	public boolean addMapping(final String coreKey, final String localKey) {
		if (mappings.containsKey(coreKey)) {
			// TODO if there is a uniqueness constraint on local_key mappings,
			// then this should be a Set, not a List
			final List<String> keys = mappings.get(coreKey);

			if (keys.contains(localKey)) {
				return false;
			} else {
				return keys.add(localKey);
			}
		} else {
			final LocalKeys keys = new LocalKeys(localKey);

			mappings.put(coreKey, keys);

			return true;
		}
	}

	public Set<String> getEntries() {
		// Defensive copy; Map.keySet() can change out from underneath you
		return Collections.unmodifiableSet(new HashSet<String>(mappings.keySet()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (mappings == null ? 0 : mappings.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AdapterMappings other = (AdapterMappings) obj;
		if (mappings == null) {
			if (other.mappings != null) {
				return false;
			}
		} else if (!mappings.equals(other.mappings)) {
			return false;
		}
		return true;
	}
}
