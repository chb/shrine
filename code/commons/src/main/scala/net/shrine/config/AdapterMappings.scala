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
final case class AdapterMappings(private var mappings: Map[String, Set[String]] = Map.empty) {

  import scala.collection.JavaConverters._
  
  def getMappings: JList[String] = mappings.keySet.toSeq.asJava

  def getMappings(globalTerm: String): JList[String] = {
    mappings.get(globalTerm).map(_.toSeq.asJava).getOrElse(JCollections.emptyList[String])
  }

  def size = mappings.size

  def addMapping(coreKey: String, localKey: String) {
    mappings += (mappings.get(coreKey) match {
      case None => (coreKey -> Set(localKey))
      case Some(localTerms) => (coreKey -> (localTerms + localKey))
    })
  }

  def getEntries: JSet[String] = mappings.keySet.asJava
  
  def jaxbable: JaxbableAdapterMappings = {
    val javaMappings = mappings.mapValues(localTerms => LocalKeys(localTerms.toSeq: _*)).asJava
    
    JaxbableAdapterMappings(new JTreeMap(javaMappings))
  }
}

object AdapterMappings {
  def apply(jaxbable: JaxbableAdapterMappings): AdapterMappings = {
    import scala.collection.JavaConverters._
    
    val mappings = jaxbable.mappings.asScala.mapValues(_.asScala.toSet).toMap
    
    AdapterMappings(mappings)
  }
}
