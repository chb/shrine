package net.shrine.webclient.server

import net.shrine.protocol.I2b2ResultEnvelope
import net.shrine.protocol.QueryResult
import net.shrine.webclient.shared.domain.SingleInstitutionQueryResult
import net.shrine.webclient.shared.domain.Breakdown
import java.util.{Map => JMap}
import java.util.{HashMap => JHashMap}


/**
 * @author clint
 * @date Sep 11, 2012
 */
object Helpers {
  import scala.collection.JavaConverters._
  
  def makeBreakdownsByTypeMap(envelopes: Iterable[I2b2ResultEnvelope]): JMap[String, Breakdown] = {
    envelopes.map(envelope => (envelope.resultType.name, makeBreakdown(envelope))).toMap.asJava
  }
  
  def makeSingleInstitutionQueryResult(result: QueryResult): SingleInstitutionQueryResult = {
    val setSize = result.setSize.toInt
    
    val breakdownsByType = makeBreakdownsByTypeMap(result.breakdowns.values)
    
    new SingleInstitutionQueryResult(setSize, breakdownsByType, result.isError)
  }
  
  def makeBreakdown(envelope: I2b2ResultEnvelope) = {
    import java.lang.Long.{valueOf => toJLong} 
    
    val values = envelope.toMap.mapValues(toJLong(_))
    
    new Breakdown(values.asJava)
  }
}