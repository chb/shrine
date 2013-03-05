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
final case class AdapterMappings(val mappings: Map[String, Set[String]] = Map.empty) {

  import scala.collection.JavaConverters._

  def getAllMappings: Set[String] = mappings.keySet

  def getMappings(globalTerm: String): Set[String] = mappings.get(globalTerm).getOrElse(Set.empty)

  def size = mappings.size

  def ++(ms: (String, String)*): AdapterMappings = ms.foldLeft(this)(_ + _)
  
  def +(mapping: (String, String)): AdapterMappings = {
    val (coreTerm, localTerm) = mapping

    mappings.get(coreTerm) match {
      case Some(localTerms) if localTerms.contains(localTerm) => this
      case possiblyExtantMapping => {
        val newLocalTerms = possiblyExtantMapping.getOrElse(Set.empty) + localTerm

        AdapterMappings(mappings + (coreTerm -> newLocalTerms))
      }
    }
  }

  def getEntries: Set[String] = mappings.keySet

  def jaxbable: JaxbableAdapterMappings = {
    val javaMappings = mappings.mapValues(localTerms => LocalKeys(localTerms.toSeq: _*)).asJava

    JaxbableAdapterMappings(new JTreeMap(javaMappings))
  }
}

object AdapterMappings {
  val empty = new AdapterMappings
  
  def apply(jaxbable: JaxbableAdapterMappings): AdapterMappings = {
    import scala.collection.JavaConverters._

    val mappings = jaxbable.mappings.asScala.mapValues(_.asScala.toSet).toMap

    AdapterMappings(mappings)
  }
}
