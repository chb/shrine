package net.shrine.adapter.dao.model

import javax.xml.datatype.XMLGregorianCalendar

/**
 * @author clint
 * @date Oct 16, 2012
 */
final case class PrivilegedUser(
  id: Int,
  username: String,
  domain: String,
  threshold: Int,
  overrideDate: XMLGregorianCalendar)