package net.shrine.protocol

import net.shrine.protocol.handlers.DeleteQueryHandler
import net.shrine.protocol.handlers.ReadApprovedTopicsHandler
import net.shrine.protocol.handlers.ReadInstanceResultsHandler
import net.shrine.protocol.handlers.ReadPdoHandler
import net.shrine.protocol.handlers.ReadPreviousQueriesHandler
import net.shrine.protocol.handlers.ReadQueryDefinitionHandler
import net.shrine.protocol.handlers.ReadQueryInstancesHandler
import net.shrine.protocol.handlers.ReadQueryResultHandler
import net.shrine.protocol.handlers.RenameQueryHandler
import net.shrine.protocol.handlers.RunQueryHandler

/**
 * @author Bill Simons
 * @date 3/9/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
trait ShrineRequestHandler extends 
	ReadPreviousQueriesHandler with
	ReadApprovedTopicsHandler with
	ReadQueryInstancesHandler with 
	ReadInstanceResultsHandler with
	ReadPdoHandler with 
	ReadQueryDefinitionHandler with 
	RunQueryHandler with 
	DeleteQueryHandler with
	RenameQueryHandler with 
	ReadQueryResultHandler
