package net.shrine.protocol

/**
 * @author clint
 * @date Mar 29, 2013
 */
trait WontComeFromI2b2Request { self: ShrineRequest =>
  override type Handler = Any
  
  //NB: Unimplemented, as this will never be sent through I2b2Resource 
  def handle(handler: Handler, shouldBroadcast: Boolean): ShrineResponse = ???
}