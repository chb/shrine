package net.shrine.adapter.dao;

import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType;
import org.spin.tools.crypto.signature.Identity;

import java.util.List;

/**
 * @author David Ortiz
 *         <p/>
 *         Interface to factor out the common methods that an adapter DAO should implement.
 */
public interface AdapterDAO {
    RequestResponseData findRequestResponseDataByResultID(long resultID) throws DAOException;

    void insertRequestResponseData(RequestResponseData requestResponseData) throws DAOException;

    void insertMasterIDPair(IDPair idPair) throws DAOException;

    void insertMaster(MasterTuple tuple) throws DAOException;

    void insertInstanceIDPair(IDPair idPair) throws DAOException;

    void insertResultTuple(ResultTuple tuple) throws DAOException;

    void insertUserAndMasterIDMapping(UserAndMaster mapping) throws DAOException;

    @SuppressWarnings("unchecked")
    List<RequestResponseData> getAuditEntries(Identity id) throws DAOException;

    List<UserAndMaster> findRecentQueries(int limit) throws DAOException;

    boolean isUserLockedOut(Identity id, Integer defaultThreshold);

    MasterQueryDefinition findMasterQueryDefinition(Long broadcastMasterID) throws DAOException;

    String findLocalMasterID(Long broadcastMasterID) throws DAOException;

    String findLocalInstanceID(Long broadcastInstanceID) throws DAOException;

    String findLocalResultID(Long broadcastResultID) throws DAOException;

    Long findNetworkMasterID(String localMasterID) throws DAOException;

    Long findNetworkInstanceID(String localInstanceID) throws DAOException;

    Long findNetworkResultID(String localResultID) throws DAOException;

    @SuppressWarnings("unchecked")
    List<QueryMasterType> findNetworkMasterDefinitions(String domainName, String userName) throws DAOException;

    Integer findObfuscationAmount(String networkResultId) throws DAOException;

    void updateObfuscationAmount(String networkResultId, int obfuscationAmount) throws DAOException;

    void removeMasterDefinitions(Long networkMasterId) throws DAOException;

    void removeUserToMasterMapping(Long networkMasterId) throws DAOException;

    int findUserThreshold(String username) throws DAOException;

    void insertUserThreshold(String username, Integer threshold) throws DAOException;

    void updateUsersToMasterQueryName(Long masterId, String queryName) throws DAOException;
}
