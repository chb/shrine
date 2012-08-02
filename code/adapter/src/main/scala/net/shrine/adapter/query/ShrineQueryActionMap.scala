package net.shrine.adapter.query

import org.spin.node.DestroyableQueryActionMap
import org.spin.node.UnknownQueryTypeException
import java.util.{ Collection => JCollection }
import java.util.{ Map => JMap }
import org.spin.node.QueryAction

/**
 * @author Bill Simons
 * @date Jul 9, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class ShrineQueryActionMap(actionMap: JMap[String, QueryAction[_]]) extends DestroyableQueryActionMap {

  override def containsQueryType(queryType: String) = actionMap.get(queryType) != null

  override def getQueryAction(queryType: String): QueryAction[_] = actionMap.get(queryType)

  override def getQueryTypes(): JCollection[String] = actionMap.keySet
}
