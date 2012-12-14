package net.shrine.utilities

import net.shrine.protocol.query.QueryDefinition
import net.shrine.client.ShrineClient
import net.shrine.protocol.ResultOutputType
import net.shrine.util.Try
import net.shrine.util.Success
import net.shrine.util.Loggable
import net.shrine.util.Failure

/**
 * @author clint
 * @date Dec 7, 2012
 */
final class QueryRunner(client: ShrineClient, queryDefs: Seq[QueryDefinition]) extends Loggable {
  private val desiredOutputTypes = Set(ResultOutputType.PATIENT_COUNT_XML)

  private val topicId = "4" //MAGIC

  def run: Seq[Command] = {
    val attempts = queryDefs.map(queryDef => (queryDef, Try(client.runQuery(topicId, desiredOutputTypes, queryDef))))

    val (successes, failures) = attempts.partition(_._2.isSuccess)

    failures.map { case (queryDef, Failure(e)) => error("Couldn't run query: " + queryDef, e) }

    successes.map {
      case (queryDef, Success(aggregatedRunQueryResponse)) =>
        Command { println(aggregatedRunQueryResponse) }
    }
  }
}
