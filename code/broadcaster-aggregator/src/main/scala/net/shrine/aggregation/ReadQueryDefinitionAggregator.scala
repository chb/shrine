package net.shrine.aggregation

import collection.mutable.ArrayBuffer
import net.shrine.protocol.{ErrorResponse, ShrineResponse, ReadQueryDefinitionResponse}
import net.shrine.util.Loggable

/**
 * @author Bill Simons
 * @date 6/14/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
final class ReadQueryDefinitionAggregator extends IgnoresErrorsAggregator[ReadQueryDefinitionResponse]