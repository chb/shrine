package net.shrine.adapter

import dao.AdapterDAO
import net.shrine.protocol.QueryResult

/**
 * @author Bill Simons
 * @date 4/21/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
object Obfuscator {
  def obfuscate(results: Seq[QueryResult], dao: AdapterDAO): Seq[QueryResult] = {
    results map {result =>
      val amount = dao.findObfuscationAmount(result.resultId.toString)
      val newSetSize =
        if(amount != null) {
	      GaussianObfuscator.determineObfuscatedSetSize(result.setSize, amount.intValue)
	    }
	    else {
	      val obfuscationAmount = GaussianObfuscator.determineObfuscationAmount(result.setSize)
	      dao.updateObfuscationAmount(result.resultId.toString, obfuscationAmount);
	      GaussianObfuscator.determineObfuscatedSetSize(result.setSize, obfuscationAmount)
	    }
      
      result.withSetSize(newSetSize)
    }
  }
}