package net.shrine.webclient.server

import net.shrine.webclient.client.domain.TermSuggestion
import net.shrine.webclient.client.domain.OntNode

/**
 * @author Clint
 * @date Aug 3, 2012
 */
trait OntologyService {
  def getSuggestions(typedSoFar: String, limit: Int): Seq[TermSuggestion]

  def getPathTo(term: String): Seq[OntNode]

  def getChildrenFor(term: String): Seq[OntNode]
}
