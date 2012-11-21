package net.shrine.adapter.dao

import org.spin.tools.crypto.signature.Identity

import java.util.{ List => JList, Date }
import net.shrine.protocol.QueryMaster

/**
 * @author David Ortiz
 *         <p/>
 *         Interface to factor out the common methods that an adapter DAO should implement.
 */
@Deprecated
trait LegacyAdapterDAO {
  def findRequestResponseDataByResultID(resultID: Long): Option[RequestResponseData]

  def insertRequestResponseData(requestResponseData: RequestResponseData): Unit

  def insertMasterIDPair(idPair: IDPair): Unit

  def insertMaster(tuple: MasterTuple): Unit

  def insertInstanceIDPair(idPair: IDPair): Unit

  def insertResultTuple(tuple: ResultTuple): Unit

  def insertUserAndMasterIDMapping(mapping: UserAndMaster): Unit

  def getAuditEntries(id: Identity): Seq[RequestResponseData]

  def findRecentQueries(limit: Int): Seq[UserAndMaster]

  def isUserLockedOut(id: Identity, defaultThreshold: Int): Boolean

  def overrideLockout(id: Identity, overrideDate: Date)

  def findMasterQueryDefinition(broadcastMasterID: Long): MasterQueryDefinition

  def findLocalMasterID(broadcastMasterID: Long): String

  def findLocalInstanceID(broadcastInstanceID: Long): String

  def findLocalResultID(broadcastResultID: Long): String

  def findNetworkMasterID(localMasterID: String): Option[Long]

  def findNetworkInstanceID(localInstanceID: String): Option[Long]

  def findNetworkResultID(localResultID: String): Option[Long]

  def findNetworkMasterDefinitions(domainName: String, userName: String): Seq[QueryMaster]

  def findObfuscationAmount(networkResultId: String): Option[Int]

  def updateObfuscationAmount(networkResultId: String, obfuscationAmount: Int): Unit

  def removeMasterDefinitions(networkMasterId: Long): Unit

  def removeUserToMasterMapping(networkMasterId: Long): Unit

  def findUserThreshold(id: Identity): java.lang.Integer

  def insertUserThreshold(id: Identity, threshold: Int): Unit

  def updateUsersToMasterQueryName(masterId: Long, queryName: String): Unit
}
