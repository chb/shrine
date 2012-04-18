package net.shrine.protocol

import javax.xml.datatype.XMLGregorianCalendar

/**
 * @author Bill Simons
 * @date 4/2/12
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
case class QueryMaster (
    val queryMasterId: String,
    val name: String,
    val userId: String,
    val groupId: String,
    val createDate: XMLGregorianCalendar)