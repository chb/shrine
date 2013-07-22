package net.shrine.service

import net.shrine.protocol.ResultOutputType
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status

/**
 *
 * @author Clint Gilbert
 * @date Sep 20, 2011
 *
 * @link http://cbmi.med.harvard.edu
 *
 * This software is licensed under the LGPL
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 * Wraps a Set[ResultOutputType], and provides a constructor that takes a String representing a
 * serialized Set[ResultOutputType].  Used by JAXRS to unmarshal params sent as Strings (@QueryParams,
 * @HeaderParams, etc.)
 *
 * NB: A case class for structural equality
 */
final case class OutputTypeSet(private val outputTypes: Set[ResultOutputType]) {
  require(outputTypes != null)
  
  def this(outputTypesString: String) = this(OutputTypeSet.deserialize(outputTypesString))

  def serialized: String = {
    require(outputTypes != null)

    import java.net.URLEncoder.encode

    encode(outputTypes.map(_.name).mkString(","))
  }

  def toSet = outputTypes
}

object OutputTypeSet {

  private[service] def deserialize(outputTypesString: String): Set[ResultOutputType] = {
    import java.net.URLDecoder.decode

    try {
      require(outputTypesString != null)
      
      if (outputTypesString == "") {
        Set.empty
      } else {
        decode(outputTypesString).split(",").map(ResultOutputType.valueOf).toSet
      }
    } catch {
      case e: Exception => throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
        .entity("Couldn't parse '" + outputTypesString + "' into a Set or ResultOutputTypes")
        .build())
    }
  }
}