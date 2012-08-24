package net.shrine.config

import java.util.{ ArrayList => JArrayList }
import java.util.{ Collections => JCollections }
import java.util.{ Collection => JCollection }
import java.util.{ Iterator => JIterator }
import java.util.{ List => JList }
import java.util.{ ListIterator => JListIterator }
import java.util.{ TreeMap => JTreeMap }
import java.util.{ HashSet => JHashSet }
import java.util.{ Set => JSet }

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlRootElement

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
final case class AdapterMappings {

  private val mappings: JTreeMap[String, LocalKeys] = new JTreeMap

  private def immutableCopy[T](coll: JCollection[T]): JList[T] = {
    JCollections.unmodifiableList(new JArrayList(coll))
  }

  def getMappings: JList[String] = immutableCopy(mappings.keySet)

  def getMappings(globalKey: String): JList[String] = {
    Option(mappings.get(globalKey)).map(immutableCopy).getOrElse(JCollections.emptyList[String])
  }

  def size = mappings.size

  def addMapping(coreKey: String, localKey: String): Boolean = {
    if (mappings.containsKey(coreKey)) {
      // TODO if there is a uniqueness constraint on local_key mappings,
      // then this should be a Set, not a List
      val keys: JList[String] = mappings.get(coreKey)

      if (keys.contains(localKey)) false else keys.add(localKey)
    } else {
      val keys = new LocalKeys(localKey)

      mappings.put(coreKey, keys)

      true
    }
  }

  def getEntries: JSet[String] = {
    // Defensive copy Map.keySet() can change out from underneath you
    JCollections.unmodifiableSet(new JHashSet(mappings.keySet))
  }
}
