package net.shrine.config

import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType

import java.util.{TreeMap => JTreeMap}
import java.util.{Map => JMap}

/**
 * @author clint
 * @date Mar 5, 2013
 */
@XmlRootElement(name = "AdapterMappings")
@XmlAccessorType(XmlAccessType.FIELD)
final case class JaxbableAdapterMappings(val mappings: JMap[String, LocalKeys]) {
  //For JAXB
  def this() = this(new JTreeMap)
}