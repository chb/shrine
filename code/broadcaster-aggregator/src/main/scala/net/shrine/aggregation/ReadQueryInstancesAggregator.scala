package net.shrine.aggregation

import net.shrine.protocol.ReadQueryInstancesResponse
import net.shrine.aggregation.BasicAggregator.Valid

/**
 * @author Bill Simons
 * @author Clint Gilbert
 * @date 6/10/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class ReadQueryInstancesAggregator(
    private val queryId: Long,
    private val username: String,
    private val projectId: String) extends IgnoresErrorsAggregator[ReadQueryInstancesResponse] {
  
  override def makeResponseFrom(validResponses: Seq[Valid[ReadQueryInstancesResponse]]) = {
     val distinctQueryInstances = validResponses.flatMap(_.response.queryInstances).distinct
    
    new ReadQueryInstancesResponse(queryId, username, projectId, distinctQueryInstances)
  }
}