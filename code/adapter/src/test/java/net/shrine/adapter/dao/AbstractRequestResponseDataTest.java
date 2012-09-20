package net.shrine.adapter.dao;

import org.spin.tools.crypto.signature.Identity;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.spin.tools.Util.makeHashSet;

/**
 * @author clint
 *         <p/>
 *         Aug 30, 2010
 *         <p/>
 *         Center for Biomedical Informatics (CBMI)
 * @link https://cbmi.med.harvard.edu/
 */
public abstract class AbstractRequestResponseDataTest extends AbstractTransactionalDataSourceSpringContextTests {
    protected final String username = "some guy";
    protected final String domain = "some place";

    protected final Identity identity1 = new Identity(domain, username);

    protected final Identity identity2 = new Identity("some other domain", "some other username");

    protected final long masterID1 = 1;
    protected final long masterID2 = 2;
    protected final long masterID3 = 3;
    protected final long masterID4 = 4;

    protected final String masterName1 = "master 1";
    protected final String masterName2 = "master 2";
    protected final String masterName3 = "master 3";
    protected final String masterName4 = "master 4";

    protected final Date masterCreateTime1 = new Date();
    protected final Date masterCreateTime2 = new Date();

    protected final long instanceID1 = 99;
    protected final long instanceID2 = 999;

    protected final long resultID1a = 12345;
    protected final long resultID1b = 98765;

    protected final String localMasterID1 = "e94y8eorut";
    protected final String localMasterID2 = "owq39tuo9usd";

    protected final String localInstanceID1 = "alskdkf";
    protected final String localInstanceID2 = "salkfhaksjhf";

    protected final String localResultID1a = "asuifyi87u38w9uksdfh";
    protected final String localResultID1b = "aslkflhkajsfh923q87rashfd";

    protected final MasterTuple master1 = new MasterTuple(IDPair.of(masterID1, localMasterID1), "<ns2:query_definition xmlns:ns2=\"http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/\">\n" + "<query_name>" + masterName1 + "</query_name></ns2:query_definition>");
    protected final MasterTuple master2 = new MasterTuple(IDPair.of(masterID2, localMasterID2), "<ns2:query_definition xmlns:ns2=\"http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/\">\n" + "<query_name>master2</query_name></ns2:query_definition>");
    protected final MasterTuple master3 = new MasterTuple(IDPair.of(masterID3, localMasterID2), "<ns2:query_definition xmlns:ns2=\"http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/\">\n" + "<query_name>master3</query_name></ns2:query_definition>");
    protected final MasterTuple master4 = new MasterTuple(IDPair.of(masterID4, localMasterID2), "<ns2:query_definition xmlns:ns2=\"http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/\">\n" + "<query_name>master4</query_name></ns2:query_definition>");

    protected final IDPair instance1 = IDPair.of(instanceID1, localInstanceID1);
    protected final IDPair instance2 = IDPair.of(instanceID2, localInstanceID2);

    protected final Integer result1aObfuscationAmount = 1;
    protected final Integer result1bObfuscationAmount = 2;
    protected final ResultTuple result1a = new ResultTuple(IDPair.of(resultID1a, localResultID1a), result1aObfuscationAmount);
    protected final ResultTuple result1b = new ResultTuple(IDPair.of(resultID1b, localResultID1b), result1bObfuscationAmount);

    protected final RequestResponseData requestResponseData1 = new RequestResponseData(domain, username, masterID1, instanceID1, resultID1a, "ERROR", 10, 1000, "spin", "<resultXml/>");
    protected final RequestResponseData requestResponseData2 = new RequestResponseData(domain, username, masterID2, instanceID2, resultID1b, "ERROR", 10, 1000, "spin", "<resultXml/>");

    protected final RequestResponseData requestResponseData3 = new RequestResponseData(domain, username, masterID3, instanceID2, resultID1b, "ERROR", 10, 1000, "spin", "<resultXml/>");
    protected final RequestResponseData requestResponseData4 = new RequestResponseData(identity2.getDomain(), identity2.getUsername(), masterID4, 1, 1, "ERROR", 10, 1000, "spin", "<resultXml/>");

    @Resource
    protected AdapterDAO adapterDAO;

    protected void insertResultIDPairs() throws DAOException {
        for (final ResultTuple tuple : asList(result1a, result1b)) {
            adapterDAO.insertResultTuple(tuple);
        }
    }

    protected void insertInstanceIDPairs() throws DAOException {
        for (final IDPair pair : asList(instance1, instance2)) {
            adapterDAO.insertInstanceIDPair(pair);
        }
    }

    protected void insertMasterQuerys() throws DAOException {
        for (final MasterTuple pair : asList(master1, master2)) {
            adapterDAO.insertMaster(pair);
        }
    }

    protected void insertRequestResponseDatas() throws DAOException {
        for (final RequestResponseData mapping : asList(requestResponseData1, requestResponseData2, requestResponseData3, requestResponseData4)) {
            adapterDAO.insertRequestResponseData(mapping);
        }
    }

    @SuppressWarnings("serial")
    private final Map<Long, String> masterIDsToNames = new HashMap<Long, String>() {
        {
            this.put(masterID1, masterName1);
            this.put(masterID2, masterName2);
            this.put(masterID3, masterName3);
            this.put(masterID4, masterName4);
        }
    };

    @SuppressWarnings("serial")
    private final Map<Long, Date> masterIDsToTimes = new HashMap<Long, Date>() {
        {
            this.put(masterID1, masterCreateTime1);
            this.put(masterID2, masterCreateTime2);
            this.put(masterID3, masterCreateTime2);
            this.put(masterID4, masterCreateTime2);
        }
    };

    protected void mapUsersToMasterIDs() throws DAOException {
        final Set<UserAndMaster> mappings = makeHashSet();

        // ensure mappings are unique
        for (final RequestResponseData requestResponseData : asList(requestResponseData1, requestResponseData2, requestResponseData3, requestResponseData4)) {
            mappings.add(new UserAndMaster(requestResponseData.getDomainName(), requestResponseData.getUsername(), requestResponseData.getBroadcastQueryMasterId(), masterIDsToNames.get(requestResponseData.getBroadcastQueryMasterId()), masterIDsToTimes.get(requestResponseData.getBroadcastQueryMasterId())));
        }

        for (final UserAndMaster mapping : mappings) {
            adapterDAO.insertUserAndMasterIDMapping(mapping);
        }
    }
}
