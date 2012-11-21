package net.shrine.adapter.dao

import org.spin.tools.crypto.signature.Identity
import java.util.Date
import net.shrine.protocol.QueryMaster

/**
 * @author clint
 * @date Sep 21, 2012
 */
object MockLegacyAdapterDao extends MockLegacyAdapterDao

class MockLegacyAdapterDao extends LegacyAdapterDAO {
  override def findRequestResponseDataByResultID(resultID: Long): Option[RequestResponseData] = None

  override def insertRequestResponseData(requestResponseData: RequestResponseData): Unit = ()

  override def insertMasterIDPair(idPair: IDPair): Unit = ()

  override def insertMaster(tuple: MasterTuple): Unit = ()

  override def insertInstanceIDPair(idPair: IDPair): Unit = ()

  override def insertResultTuple(tuple: ResultTuple): Unit = ()

  override def insertUserAndMasterIDMapping(mapping: UserAndMaster): Unit = ()

  override def getAuditEntries(id: Identity): Seq[RequestResponseData] = Nil

  override def findRecentQueries(limit: Int): Seq[UserAndMaster] = Nil

  override def isUserLockedOut(id: Identity, defaultThreshold: Int): Boolean = false

  override def overrideLockout(id: Identity, overrideDate: Date) = ()

  override def findMasterQueryDefinition(broadcastMasterID: Long): MasterQueryDefinition = null

  override def findLocalMasterID(broadcastMasterID: Long): String = null

  override def findLocalInstanceID(broadcastInstanceID: Long): String = null

  override def findLocalResultID(broadcastResultID: Long): String = null

  override def findNetworkMasterID(localMasterID: String): Option[Long] = None

  override def findNetworkInstanceID(localInstanceID: String): Option[Long] = None

  override def findNetworkResultID(localResultID: String): Option[Long] = None

  override def findNetworkMasterDefinitions(domainName: String, userName: String): Seq[QueryMaster] = Nil

  override def findObfuscationAmount(networkResultId: String): Option[Int] = None

  override def updateObfuscationAmount(networkResultId: String, obfuscationAmount: Int): Unit = ()

  override def removeMasterDefinitions(networkMasterId: Long): Unit = ()

  override def removeUserToMasterMapping(networkMasterId: Long): Unit = ()

  override def findUserThreshold(id: Identity): java.lang.Integer = -1

  override def insertUserThreshold(id: Identity, threshold: Int): Unit = ()

  override def updateUsersToMasterQueryName(masterId: Long, queryName: String): Unit = ()
}
