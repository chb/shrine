package net.shrine.aggregation

import collection.mutable.ArrayBuffer
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType
import javax.xml.datatype.DatatypeConstants
import net.shrine.protocol.{ ErrorResponse, ShrineResponse, ReadPreviousQueriesResponse }
import net.shrine.util.Loggable
import javax.xml.datatype.XMLGregorianCalendar
import net.shrine.aggregation.BasicAggregator.Valid

/**
 * @author Bill Simons
 * @author Clint Gilbert
 * @date 6/8/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
class ReadPreviousQueriesAggregator(
    private val userId: String,
    private val groupId: String) extends IgnoresErrorsAggregator[ReadPreviousQueriesResponse] {

  private[aggregation] def newestToOldest(x: QueryMasterType, y: QueryMasterType) = x.getCreateDate.compare(y.getCreateDate) > 0
  
  private[aggregation] def oldestToNewest(x: QueryMasterType, y: QueryMasterType) = x.getCreateDate.compare(y.getCreateDate) < 0

  override def makeResponseFrom(responses: Seq[Valid[ReadPreviousQueriesResponse]]): ShrineResponse = {
    val mastersGroupedById = responses.flatMap(_.response.queryMasters).groupBy(_.getQueryMasterId)

    val sortedMastersById = mastersGroupedById.map { case (id, mastersWithThatId) => (id, mastersWithThatId.sortWith(oldestToNewest)) }.toMap

    val mostRecentMasters = sortedMastersById.map { case (id, mastersWithThatId) => mastersWithThatId.headOption }.flatten.toSeq

    val sortedMasters = mostRecentMasters.sortWith(newestToOldest)
    
    new ReadPreviousQueriesResponse(userId, groupId, sortedMasters)
  }
}