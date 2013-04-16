package net.shrine.aggregation

import net.shrine.aggregation.BasicAggregator.Valid
import net.shrine.protocol.{QueryMaster, ShrineResponse, ReadPreviousQueriesResponse}
import net.shrine.util.Loggable

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
    private val groupId: String) extends IgnoresErrorsAggregator[ReadPreviousQueriesResponse] with Loggable {

  private[aggregation] def newestToOldest(x: QueryMaster, y: QueryMaster) = x.createDate.compare(y.createDate) > 0
  
  private[aggregation] def oldestToNewest(x: QueryMaster, y: QueryMaster) = x.createDate.compare(y.createDate) < 0

  override def makeResponseFrom(responses: Seq[Valid[ReadPreviousQueriesResponse]]): ShrineResponse = {
    //debug("Raw previous query responses: " + responses)
    
    val mastersGroupedById = responses.flatMap(_.response.queryMasters).groupBy(_.queryMasterId)

    val sortedMastersById = mastersGroupedById.map { case (id, mastersWithThatId) => (id, mastersWithThatId.sortWith(oldestToNewest)) }.toMap

    val mostRecentMastersForEachId = sortedMastersById.flatMap { case (id, mastersWithThatId) => mastersWithThatId.headOption }.toSeq

    val sortedMasters = mostRecentMastersForEachId.sortWith(newestToOldest)
    
    val result = new ReadPreviousQueriesResponse(Option(userId), Option(groupId), sortedMasters)
    
    //debug("Previous queries: ")
    
    //sortedMasters.foreach(debug(_))
    
    result
  }
}