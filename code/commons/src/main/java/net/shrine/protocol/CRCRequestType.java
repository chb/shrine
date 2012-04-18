package net.shrine.protocol;

/**
 * Simple enum listing the types of CRC requests that Shrine knows how to
 * handle. The enum names are deliberately the same as the jaxb generated class
 * of the request.
 *
 * @author Justin Quan
 * @date Jun 14, 2010
 * @link http://cbmi.med.harvard.edu
 * @link http://chip.org
 * <p/>
 * NOTICE: This software comes with NO guarantees whatsoever and is
 * licensed as Lgpl Open Source
 * @link http://www.gnu.org/licenses/lgpl.html
 */
public enum CRCRequestType
{
    GetPDOFromInputListRequestType,
    InstanceRequestType,
    MasterRequestType,
    QueryDefinitionRequestType,
    UserRequestType,
    SheriffRequestType,
    ResultRequestType,
    MasterDeleteRequestType,
    MasterRenameRequestType,
    GetRequestXml;
}
