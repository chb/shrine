package net.shrine.utilities

import net.shrine.client.JerseyShrineClient
import net.shrine.protocol.AuthenticationInfo
import net.shrine.protocol.Credential
import net.shrine.protocol.query.QueryDefinition
import net.shrine.protocol.query.Term

/**
 * @author clint
 * @date Dec 7, 2012
 */
object QueryRunnerMain {
  def main(args: Array[String]) {
    val url = "https://shrine-dev1.chip.org:6060/shrine-cell/rest/"
    val projectId = "SHRINE"
    val authn = AuthenticationInfo("HarvardDemo", "bsimons", Credential("testtest", false))
      
    val client = new JerseyShrineClient(url, projectId, authn, true)
    
    val queryDefs = Seq(QueryDefinition("foo", Term("""\\SHRINE\SHRINE\Demographics\Gender\Male\""")))
    
	val queryRunner = new QueryRunner(client, queryDefs)
    
    val commands = queryRunner.run
    
    commands.foreach(_.perform())
  }
}