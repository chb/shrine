package net.shrine.adapter.dao.hibernate

import net.shrine.adapter.dao.AdapterDAO
import net.shrine.adapter.dao.DAOException
import net.shrine.adapter.dao.IDPair
import net.shrine.adapter.dao.MasterQueryDefinition
import net.shrine.adapter.dao.MasterTuple
import net.shrine.adapter.dao.RequestResponseData
import net.shrine.adapter.dao.ResultTuple
import net.shrine.adapter.dao.UserAndMaster
import net.shrine.adapter.dao.hibernate.entity.InstanceIdsEntity
import net.shrine.adapter.dao.hibernate.entity.MasterQueryEntity
import net.shrine.adapter.dao.hibernate.entity.PrivilegedUserEntity
import net.shrine.adapter.dao.hibernate.entity.RequestResponseDataEntity
import net.shrine.adapter.dao.hibernate.entity.ResultIdsEntity
import net.shrine.adapter.dao.hibernate.entity.UsersToMasterQueryEntity
import org.apache.log4j.Logger
import org.hibernate.HibernateException
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions
import org.spin.tools.NetworkTime
import org.spin.tools.crypto.signature.Identity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

import java.sql.Timestamp
import java.util.{ArrayList => JArrayList}
import java.util.Calendar
import java.util.{Collection => JCollection}
import java.util.{Collections => JCollections}
import java.util.Date
import java.util.{List => JList}


import scala.collection.JavaConverters._
import net.shrine.protocol.QueryMaster

object HibernateAdapterDAO {
  private val log = Logger.getLogger(classOf[HibernateAdapterDAO])
}

@Repository
class HibernateAdapterDAO extends AdapterDAO {

  import HibernateAdapterDAO._

  @Autowired
  private var sessionFactory: SessionFactory = _

  private def withCurrentSession[T](body: Session => T): T = body(sessionFactory.getCurrentSession())

  private def withCurrentSession[T](errorMessage: String = "Error querying")(body: Session => T): T = {
    try {
      body(sessionFactory.getCurrentSession())
    } catch {
      case e: HibernateException => throw new DAOException(errorMessage, e)
    }
  }

  @throws(classOf[DAOException])
  override def findRequestResponseDataByResultID(resultID: Long): Option[RequestResponseData] = {
    withCurrentSession("Error querying database") {
      session =>
        val criteria = session.createCriteria(classOf[RequestResponseDataEntity]).
            add(Restrictions.eq("broadcastResultInstanceId", resultID))

        val resultList = criteria.list().asInstanceOf[JList[RequestResponseDataEntity]]

        resultList.size match {
          case x if x > 1 => throw new DAOException("Database is in inconsistent state")
          case 0 => None
          case _ => Option(EntityUtil.convertFromEntity(resultList.get(0)))
        }
    }
  }

  @throws(classOf[DAOException])
  override def insertRequestResponseData(requestResponseData: RequestResponseData) {
    val entity = EntityUtil.convertToEntity(requestResponseData)

    create(entity)
  }

  @throws(classOf[DAOException])
  override def insertMasterIDPair(idPair: IDPair) {
    val entity = new MasterQueryEntity

    entity.setBroadcastQueryMasterId(idPair.getNetworkID)
    entity.setLocalQueryMasterId(idPair.getLocalID)

    create(entity)
  }

  @throws(classOf[DAOException])
  override def insertMaster(tuple: MasterTuple) {
    val entity = EntityUtil.convertToEntity(tuple)

    create(entity)
  }

  @throws(classOf[DAOException])
  override def insertInstanceIDPair(idPair: IDPair) {
    val entity = new InstanceIdsEntity

    entity.setBroadcastQueryInstanceId(idPair.getNetworkID)
    entity.setLocalQueryInstanceId(idPair.getLocalID)

    create(entity)
  }

  @throws(classOf[DAOException])
  override def insertResultTuple(tuple: ResultTuple) {
    val entity = new ResultIdsEntity

    entity.setObfuscationAmount(tuple.getObfuscationAmount)
    entity.setBroadcastResultInstanceId(tuple.getIdPair.getNetworkID)
    entity.setLocalResultInstanceId(tuple.getLocalID)

    create(entity)
  }

  @throws(classOf[DAOException])
  override def insertUserAndMasterIDMapping(mapping: UserAndMaster) {
    val entity = new UsersToMasterQueryEntity

    entity.setBroadcastQueryMasterId(mapping.getNetworkMasterID)
    entity.setDomainName(mapping.getDomainName)
    entity.setMasterCreateDate(new Timestamp(mapping.getMasterCreateDate.getTime))
    entity.setMasterName(mapping.getMasterName)
    entity.setUsername(mapping.getUserName)

    create(entity)
  }

  @throws(classOf[DAOException])
  override def getAuditEntries(id: Identity): Seq[RequestResponseData] = {
    getAuditEntryEntities(id).asScala.map(EntityUtil.convertFromEntity)
  }

  @throws(classOf[DAOException])
  override def findRecentQueries(limit: Int): Seq[UserAndMaster] = {
    withCurrentSession("Error finding recent queries") {
      session =>
        val recentQueries = session.createCriteria(classOf[UsersToMasterQueryEntity])
            .addOrder(Order.desc("masterCreateDate"))
            .setMaxResults(limit)
            .list().asInstanceOf[JList[UsersToMasterQueryEntity]]

        recentQueries.asScala.map(query => new UserAndMaster(query.getDomainName, query.getUsername, query.getBroadcastQueryMasterId, query.getMasterName, new Date(query.getMasterCreateDate.getTime)))
    }
  }

  @throws(classOf[DAOException])
  private def getAuditEntryEntities(id: Identity): JList[RequestResponseDataEntity] = {
    withCurrentSession("Error getting audit entries") {
      sessionToUse =>
        val criteria = sessionToUse.createCriteria(classOf[RequestResponseDataEntity])

        criteria.add(Restrictions.eq("username", id.getUsername))
        criteria.add(Restrictions.eq("domainName", id.getDomain))

        criteria.list().asInstanceOf[JList[RequestResponseDataEntity]]
    }
  }

  //TODO: Throw DAOException like everything else?
  override def isUserLockedOut(id: Identity, defaultThreshold: Int): Boolean = {
    withCurrentSession {
      session =>
        try {
          val query = session.createQuery("from PrivilegedUserEntity user " +
              "where user.username = :username and user.domain = :domain")

          query.setString("username", id.getUsername)
          query.setString("domain", id.getDomain)

          val entity = query.uniqueResult().asInstanceOf[PrivilegedUserEntity]

          val thirtyDaysInThePast = {
            val today = Calendar.getInstance
            today.add(Calendar.DAY_OF_MONTH, -30)
            today.getTime
          }

          val threshold = Option(entity).map(_.getThreshold).getOrElse(defaultThreshold.intValue)
          //TODO -can we calculate the override date in one line?
          var overrideDate = Option(entity).map(_.getOverrideDate).getOrElse(null)
          overrideDate = if(overrideDate == null) thirtyDaysInThePast else overrideDate


          val query1 = session.createQuery("select count(e.resultSetSize) as setSize from RequestResponseDataEntity e" +
              " where e.username = :username" +
              " AND e.resultSetSize != 0" +
              " AND e.queryDatetime > :overrideDate" +
              " group by e.resultSetSize")

          query1.setString("username", id.getUsername)
          query1.setDate("overrideDate", overrideDate)

          val counts = query1.list().asInstanceOf[JList[Long]].asScala.sorted

          val repeatedResultCount = if(counts.size >= 1) counts.last.intValue else 0

          repeatedResultCount > threshold

        } catch {
          case e => {
            log.error("Error determining lockout status; declaring user is locked.", e)

            true
          }
        }
    }
  }

  @throws(classOf[DAOException])
  override def findMasterQueryDefinition(broadcastMasterID: Long): MasterQueryDefinition = {
    withCurrentSession("Unexpected Error") {
      session =>
        val q = session.createQuery("from MasterQueryEntity master where " +
            "master.broadcastQueryMasterId = :broadcastMasterId  ")

        q.setLong("broadcastMasterId", broadcastMasterID)

        val result = q.uniqueResult().asInstanceOf[MasterQueryEntity]

        result match {
          case null => null
          case _ => {
            val q2 = session.createQuery("from UsersToMasterQueryEntity entity " +
                "where entity.broadcastQueryMasterId = :masterQueryId")
            q2.setLong("masterQueryId", result.getBroadcastQueryMasterId())

            def toMasterQueryDefinition(usersToMaster: UsersToMasterQueryEntity): MasterQueryDefinition = {
              val returnValue = new MasterQueryDefinition

              returnValue.setUserId(usersToMaster.getUsername)
              returnValue.setGroupId(usersToMaster.getDomainName)
              returnValue.setName(usersToMaster.getMasterName)
              returnValue.setCreateDate(usersToMaster.getMasterCreateDate)
              returnValue.setQueryMasterId(result.getLocalQueryMasterId)
              returnValue.setRequestXml(result.getQueryDefinition)

              returnValue
            }

            val usersToMaster = Option(q2.uniqueResult().asInstanceOf[UsersToMasterQueryEntity])

            usersToMaster.map(toMasterQueryDefinition).getOrElse(throw new DAOException("Inconsistent database"))
          }
        }
    }
  }

  @throws(classOf[DAOException])
  override def findLocalMasterID(broadcastMasterID: Long): String = {
    withCurrentSession() {
      session =>
        val query = session.createQuery("select entity.localQueryMasterId from MasterQueryEntity entity " +
            "where entity.broadcastQueryMasterId = :masterId")

        query.setLong("masterId", broadcastMasterID)

        query.uniqueResult().asInstanceOf[String]
    }
  }

  @throws(classOf[DAOException])
  override def findLocalInstanceID(broadcastInstanceID: Long): String = {
    withCurrentSession() {
      session =>
        val query = session.createQuery("select entity.localQueryInstanceId from InstanceIdsEntity entity " +
            "where entity.broadcastQueryInstanceId = :broadcastInstanceID")

        query.setLong("broadcastInstanceID", broadcastInstanceID)

        query.uniqueResult().asInstanceOf[String]
    }
  }

  @throws(classOf[DAOException])
  override def findLocalResultID(broadcastResultID: Long): String = {
    withCurrentSession() {
      session =>
        val query = session.createQuery("select entity.localResultInstanceId from ResultIdsEntity entity " +
            "where entity.broadcastResultInstanceId = :broadcastResultId")

        query.setLong("broadcastResultId", broadcastResultID)
        query.uniqueResult().asInstanceOf[String]
    }
  }

  @throws(classOf[DAOException])
  override def findNetworkMasterID(localMasterID: String): Option[Long] = {
    withCurrentSession() {
      session =>
        val query = session.createQuery("select entity.broadcastQueryMasterId from MasterQueryEntity entity " +
            "where entity.localQueryMasterId = :localMasterId")
        query.setString("localMasterId", localMasterID)

        toOption(query.uniqueResult().asInstanceOf[java.lang.Long])
    }
  }

  private[this] def toOption(l: java.lang.Long): Option[Long] = Option(l).map(_.longValue)

  private[this] def toOption(l: java.lang.Integer): Option[Int] = Option(l).map(_.intValue)

  @throws(classOf[DAOException])
  override def findNetworkInstanceID(localInstanceID: String): Option[Long] = {
    withCurrentSession() {
      session =>
        val query = session.createQuery("select entity.broadcastQueryInstanceId from InstanceIdsEntity entity " +
            "where entity.localQueryInstanceId = :localInstanceId ")

        query.setString("localInstanceId", localInstanceID)

        toOption(query.uniqueResult().asInstanceOf[java.lang.Long])
    }
  }

  @throws(classOf[DAOException])
  override def findNetworkResultID(localResultID: String): Option[Long] = {
    withCurrentSession() {
      session =>
        val query = session.createQuery("select entity.broadcastResultInstanceId from ResultIdsEntity entity " +
            "where entity.localResultInstanceId = :localResultInstanceId")

        query.setString("localResultInstanceId", localResultID)

        toOption(query.uniqueResult().asInstanceOf[java.lang.Long])
    }
  }

  @throws(classOf[DAOException])
  override def findNetworkMasterDefinitions(domainName: String, userName: String): Seq[QueryMaster] = {
    withCurrentSession() {
      session =>
        val q = session.createQuery("select userToMaster from UsersToMasterQueryEntity " +
            "userToMaster " +
            "where userToMaster.username = :username " +
            "and userToMaster.domainName = :domainName order by userToMaster.masterCreateDate desc")

        q.setString("username", userName)
        q.setString("domainName", domainName)

        def toQueryMaster(entity: UsersToMasterQueryEntity): QueryMaster = {
          val masterId = String.valueOf(entity.getBroadcastQueryMasterId)
          new QueryMaster(masterId, entity.getMasterName, entity.getUsername, entity.getDomainName, NetworkTime.makeXMLGregorianCalendar(entity.getMasterCreateDate))
        }

        q.list().asInstanceOf[JList[UsersToMasterQueryEntity]].asScala.map(toQueryMaster)
    }
  }

  @throws(classOf[DAOException])
  override def findObfuscationAmount(networkResultId: String): Option[Int] = {
    withCurrentSession() {
      session =>
        val q = session.createQuery("select entity.obfuscationAmount from ResultIdsEntity entity " +
            "where entity.broadcastResultInstanceId = :networkResultId")

        q.setString("networkResultId", networkResultId)

        toOption(q.uniqueResult().asInstanceOf[java.lang.Integer])
    }
  }

  @throws(classOf[DAOException])
  override def updateObfuscationAmount(networkResultId: String, obfuscationAmount: Int) {
    withCurrentSession() {
      session =>
        val q = session.createQuery("from ResultIdsEntity entity " +
            "where entity.broadcastResultInstanceId = :networkResultId")

        q.setString("networkResultId", networkResultId)

        val result = q.uniqueResult().asInstanceOf[ResultIdsEntity]

        result.setObfuscationAmount(obfuscationAmount)

        session.saveOrUpdate(result)
    }
  }

  @throws(classOf[DAOException])
  override def removeMasterDefinitions(networkMasterId: Long) {
    withCurrentSession("Error saving object to database") {
      session =>
        val q = session.createQuery("delete from MasterQueryEntity master where master.broadcastQueryMasterId = :id")
        q.setLong("id", networkMasterId)
        q.executeUpdate()
    }
  }

  @throws(classOf[DAOException])
  override def removeUserToMasterMapping(networkMasterId: Long) {
    withCurrentSession("Error saving object to database") {
      session =>
        val q = session.createQuery("delete from UsersToMasterQueryEntity master where master.broadcastQueryMasterId = :id")
        q.setLong("id", networkMasterId)
        q.executeUpdate()
    }
  }

  @throws(classOf[DAOException])
  override def updateUsersToMasterQueryName(networkMasterId: Long, queryName: String) {
    withCurrentSession("Error saving object to database") {
      session =>
        val q = session.createQuery("from UsersToMasterQueryEntity entity " +
            "where entity.broadcastQueryMasterId= :id")

        q.setLong("id", networkMasterId)

        val result = q.uniqueResult().asInstanceOf[UsersToMasterQueryEntity]

        result.setMasterName(queryName)

        session.saveOrUpdate(result)
    }
  }

  @throws(classOf[DAOException])
  override def findUserThreshold(id: Identity): java.lang.Integer = {
    withCurrentSession() {
      session =>
        val q = session.createQuery("select entity.threshold from PrivilegedUserEntity entity " +
            "where entity.username = :username and entity.domain = :domain")

        q.setString("username", id.getUsername())
        q.setString("domain", id.getDomain())

        q.uniqueResult().asInstanceOf[java.lang.Integer]
    }
  }

  //TODO - too similar to insertUserThreshold - refactor
  override def overrideLockout(id: Identity, overrideDate: Date) {
      withCurrentSession("Unable to override lockout") {
        session =>
          val q = session.createQuery("from PrivilegedUserEntity p where p.username = :userName and p.domain = :domain")
          q.setString("userName", id.getUsername)
          q.setString("domain", id.getDomain)

          val entity = q.uniqueResult().asInstanceOf[PrivilegedUserEntity]

          if(entity == null) {
            val toSave = new PrivilegedUserEntity

            toSave.setOverrideDate(overrideDate)
            toSave.setUsername(id.getUsername)
            toSave.setDomain(id.getDomain)

            session.save(toSave)
          } else {
            entity.setOverrideDate(overrideDate)

            session.saveOrUpdate(entity)
          }
      }
    }

  @throws(classOf[DAOException])
  override def insertUserThreshold(id: Identity, threshold: Int) {
    withCurrentSession("Error saving object to database") {
      session =>
        val q = session.createQuery("from PrivilegedUserEntity p where p.username = :userName and p.domain = :domain")
        q.setString("userName", id.getUsername)
        q.setString("domain", id.getDomain)

        val entity = q.uniqueResult().asInstanceOf[PrivilegedUserEntity]

        if(entity == null) {
          val toSave = new PrivilegedUserEntity

          toSave.setThreshold(threshold)
          toSave.setUsername(id.getUsername)
          toSave.setDomain(id.getDomain)

          session.save(toSave)
        } else {
          entity.setThreshold(threshold)

          session.saveOrUpdate(entity)
        }
    }
  }

  @throws(classOf[DAOException])
  private def create(o: AnyRef) {
    withCurrentSession("Error saving object to database")(_.saveOrUpdate(o))
  }
}
