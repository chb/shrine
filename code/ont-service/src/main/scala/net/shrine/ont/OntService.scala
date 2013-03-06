package net.shrine.ont

import net.shrine.ont.index.OntologyIndex
import net.shrine.ont.messaging._
import net.shrine.ont.messaging.SearchResponse

/**
 * @author Justin Quan
 * @date 9/1/11
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class OntService(cache: OntologyIndex) extends OntRequestHandler {
  def search(request: String) = new SearchResponse(request, cache.search(request))
}