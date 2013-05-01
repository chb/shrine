package net.shrine.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

/**
 * @author Bill Simons
 * @date 8/10/11
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 *       <p/>
 *       NOTICE: This software comes with NO guarantees whatsoever and is
 *       licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
@Path("/happy")
@Produces(Array(MediaType.APPLICATION_XML))
@Component
@Scope("singleton")
class HappyShrineResource @Autowired() (private val happyService: HappyShrineRequestHandler) {
  @GET
  @Path("keystore")
  def keystoreReport: String = happyService.keystoreReport

  @GET
  @Path("routing")
  def routingReport: String = happyService.routingReport

  @GET
  @Path("hive")
  def hiveReport: String = happyService.hiveReport

  @GET
  @Path("spin")
  def spinReport: String = happyService.spinReport

  @GET
  @Path("adapter")
  def adapterReport: String = happyService.adapterReport

  @GET
  @Path("audit")
  def auditReport: String = happyService.auditReport

  @GET
  @Path("queries")
  def queryReport: String = happyService.queryReport

  @GET
  @Path("version")
  def versionReport: String = happyService.versionReport

  @GET
  @Path("all")
  def all: String = happyService.all
}