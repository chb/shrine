package net.shrine.service

import junit.framework.TestCase
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.junit.ShouldMatchersForJUnit
import org.spin.tools.NetworkTime
import scala.xml.Elem
import scala.xml.NodeSeq
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryMasterType
import org.spin.tools.RandomTool
import com.sun.jersey.api.client.config.DefaultClientConfig
import com.sun.jersey.client.urlconnection.HTTPSProperties
import com.sun.jersey.api.client.Client
import net.shrine.protocol._

/**
 *
 * @author Clint Gilbert
 * @date Sep 19, 2011
 *
 * @link http://cbmi.med.harvard.edu
 *
 * This software is licensed under the LGPL
 * @link http://www.gnu.org/licenses/lgpl.html
 *
 * A client for remote ShrineResources, implemented using Jersey
 *
 */
final class JerseyShrineClientTest extends TestCase with AssertionsForJUnit with ShouldMatchersForJUnit {
  
  private val uri = "http://example.com"
  private val projectId = "alkjdasld"
  private val authn = new AuthenticationInfo("domain", "user", new Credential("skdhaskdhkaf", true))
  
  def testConstructor {
    val uri = "http://example.com"
    val projectId = "alkjdasld"
    val authn = new AuthenticationInfo("domain", "user", new Credential("skdhaskdhkaf", true))
    
    def doTestConstructor(client: JerseyShrineClient, expectedAcceptAllCertsValue: Boolean) {
      client should not be(null)
      client.shrineUrl should equal(uri)
      client.authorization should equal(authn)
      client.projectId should equal(projectId)
      client.acceptAllSslCerts should equal(expectedAcceptAllCertsValue)
    }
      
    doTestConstructor(new JerseyShrineClient(uri, projectId, authn, false), false)
    doTestConstructor(new JerseyShrineClient(uri, projectId, authn, true), true)
    //default value for acceptAllSslCerts should be false
    doTestConstructor(new JerseyShrineClient(uri, projectId, authn), false)
    
    intercept[IllegalArgumentException] {
      new JerseyShrineClient(null, projectId, authn)
    }
    
    intercept[IllegalArgumentException] {
      new JerseyShrineClient("aslkdfjaklsf", projectId, authn)
    }
    
    intercept[IllegalArgumentException] {
      new JerseyShrineClient(uri, null, authn)
    }
    
    intercept[IllegalArgumentException] {
      new JerseyShrineClient("aslkdfjaklsf", projectId, null)
    }
  }
  
  def testIsValidUrl {
    import JerseyShrineClient.isValidUrl

    isValidUrl(null) should be(false)
    isValidUrl("") should be(false)
    isValidUrl("aksfhkasfh") should be(false)
    isValidUrl("example.com") should be(false)

    isValidUrl("http://example.com") should be(true)
  }

  def testTrustsAllCertsHostnameVerifier {
    import JerseyShrineClient.TrustsAllCertsHostnameVerifier._

    //These assertions aren't great, but they're about the best we can do;
    //TrustsAllCertsHostnameVerifier should return true for all input
    verify(null, null) should equal(true)
    verify("", null) should equal(true)
    verify("asklfjalksf", null) should equal(true)
  }

  def testTrustsAllCertsTrustManager {
    import JerseyShrineClient.TrustsAllCertsTrustManager._

    getAcceptedIssuers should be(null)

    //We can't prove that these two don't have side effects, but we can check that they don't throw 
    checkClientTrusted(Array(), "")
    checkServerTrusted(Array(), "")
  }

  def testCreateClientAndWebResource {
    import JerseyShrineClient.createClient
    import scala.collection.JavaConverters._

    val defaultClientConfig = new DefaultClientConfig

    type HasProperties = {
      def getProperties(): java.util.Map[String, AnyRef]
    }

    def doChecksCertsClientTest(client: HasProperties) {
      client should not be (null)
      //check that we only have default properties
      //turn property maps to Scala maps to get workable equals()
      client.getProperties.asScala should equal(defaultClientConfig.getProperties.asScala)
    }

    def doTrustsAllCertsClientTest(client: HasProperties) {
      client should not be (null)

      import HTTPSProperties.{ PROPERTY_HTTPS_PROPERTIES => httpsPropsKey }

      val clientProps = client.getProperties.asScala
      val propertiesWithoutHttpsProperties = clientProps - httpsPropsKey
      val httpsProperties = clientProps(httpsPropsKey).asInstanceOf[HTTPSProperties]

      propertiesWithoutHttpsProperties should equal(defaultClientConfig.getProperties.asScala)

      httpsProperties should not be (null)
      httpsProperties.getHostnameVerifier should be(JerseyShrineClient.TrustsAllCertsHostnameVerifier)
      httpsProperties.getSSLContext should not be (null)
      httpsProperties.getSSLContext.getProtocol should equal("TLS")
      //Would be nice to test that the SSLContext correctly uses TrustsAllCertsTrustManager, but this doesn't seem possible
    }

    {
      doChecksCertsClientTest(createClient(acceptAllSslCerts = false))

      val webResource = new JerseyShrineClient(uri, projectId, authn, acceptAllSslCerts = false).webResource
      
      doChecksCertsClientTest(webResource)
      webResource.getURI.toString should equal(uri)
    }

    {
      doTrustsAllCertsClientTest(createClient(acceptAllSslCerts = true))
      
      val webResource = new JerseyShrineClient(uri, projectId, authn, acceptAllSslCerts = true).webResource
      
      doTrustsAllCertsClientTest(webResource)
      webResource.getURI.toString should equal(uri)
    }
  }

  def testPerform {
    final case class Foo(x: String) {
      def toXml = <Foo><x>{ x }</x></Foo>
    }

    implicit def fooDeserializer(xml: NodeSeq): Foo = new Foo((xml \ "x").text)

    val value = "laskjdasjklfhkasf"

    val client = new JerseyShrineClient(uri, projectId, authn, acceptAllSslCerts = false)
      
    val unmarshalled: Foo = client.perform(client.webResource, _ => Foo(value).toXml.toString)

    unmarshalled should not be (null)

    val Foo(unmarshalledValue) = unmarshalled

    unmarshalledValue should equal(value)
  }

  def testDeserializers {
    def doTestDeserializer[T <: ShrineResponse](response: T, deserialize: NodeSeq => T) {
      val roundTripped = deserialize(response.toXml)

      roundTripped should equal(response)
    }

    import ShrineResource.waitTimeMs 

    doTestDeserializer(new RunQueryResponse(123L, now, "userId", "groupId", "<requestXml/>", 456L, Seq.empty), JerseyShrineClient.runQueryResponseDeserializer)

    doTestDeserializer(new ReadApprovedQueryTopicsResponse(Seq(new ApprovedTopic(123L, "asjkhjkas"))), JerseyShrineClient.readApprovedQueryTopicsResponseDeserializer)

    doTestDeserializer(new ReadPreviousQueriesResponse("userId", "groupId", Seq.empty), JerseyShrineClient.readPreviousQueriesResponseDeserializer)

    doTestDeserializer(new ReadQueryInstancesResponse(999L, "userId", "groupId", Seq.empty), JerseyShrineClient.readQueryInstancesResponseDeserializer)

    doTestDeserializer(new ReadInstanceResultsResponse(1337L, Seq(dummyQueryResult(1337L))), JerseyShrineClient.readInstanceResultsResponseDeserializer)

    doTestDeserializer(new ReadPdoResponse(Seq(new EventResponse("event", "patient", None, None, Seq.empty)), Seq(new PatientResponse("patientId", Seq(paramResponse))), Seq(new ObservationResponse(None, "eventId", None, "patientId", None, None, None, "observerCode", "startDate", None, "valueTypeCode",None,None,None,None,None,None,None, Seq(paramResponse)))), JerseyShrineClient.readPdoResponseDeserializer)

    doTestDeserializer(new ReadQueryDefinitionResponse(87456L, "name", "userId", now, "<foo/>"), JerseyShrineClient.readQueryDefinitionResponseDeserializer)

    doTestDeserializer(new DeleteQueryResponse(56834756L), JerseyShrineClient.deleteQueryResponseDeserializer)

    doTestDeserializer(new RenameQueryResponse(56834756L, "some-name"), JerseyShrineClient.renameQueryResponseDeserializer)
  }

  private def now = (new NetworkTime).getXMLGregorianCalendar

  private def dummyQueryResult(enclosingInstanceId: Long) = new QueryResult(123L, enclosingInstanceId, "resultType", 789L, None, None, Some("description"), "statusType", Some("statusMessage"))

  import RandomTool.randomString

  private def paramResponse = new ParamResponse(randomString, randomString, randomString)
}