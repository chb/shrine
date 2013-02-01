package net.shrine.integration

import org.spin.node.NodeException
import org.spin.node.SpinNode
import org.spin.node.connector.NodeConnector

/**
 *
 * @author Clint Gilbert
 *
 *         Jan 20, 2010
 *
 *         Center for Biomedical Informatics (CBMI)
 *
 */
object LocalNodeMap {
  def empty[N <: SpinNode] = new LocalNodeMap[N](Map.empty)
}

final case class LocalNodeMap[N <: SpinNode](nodes: Map[NodeName, N]) {

  def getNodeConnector(nodeName: NodeName): NodeConnector = NodeConnector.instance(nodes(nodeName))

  def contains(key: NodeName): Boolean = nodes.contains(key)

  def get(key: NodeName): N = nodes(key)

  def +(mapping: (NodeName, N)): LocalNodeMap[N] = this.copy(nodes = nodes + mapping)

  def values: Set[N] = nodes.values.toSet
}
