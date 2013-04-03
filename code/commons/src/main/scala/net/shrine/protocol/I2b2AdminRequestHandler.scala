package net.shrine.protocol

import net.shrine.protocol.handlers.ReadI2b2AdminPreviousQueriesHandler
import net.shrine.protocol.handlers.ReadQueryDefinitionHandler

/**
 * @author clint
 * @date Apr 1, 2013
 */
trait I2b2AdminRequestHandler extends ReadI2b2AdminPreviousQueriesHandler with ReadQueryDefinitionHandler