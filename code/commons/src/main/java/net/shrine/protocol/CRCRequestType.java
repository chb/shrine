package net.shrine.protocol;

import scala.Option;

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
public enum CRCRequestType {
    GetPDOFromInputListRequestType("getPDO_fromInputList"),
    InstanceRequestType("CRC_QRY_getQueryResultInstanceList_fromQueryInstanceId"),
    MasterRequestType("CRC_QRY_getQueryInstanceList_fromQueryMasterId"),
    QueryDefinitionRequestType("CRC_QRY_runQueryInstance_fromQueryDefinition"),
    UserRequestType("CRC_QRY_getQueryMasterList_fromUserId"),
    SheriffRequestType(null),
    ResultListRequestType("CRC_QRY_getQueryResultInstanceList_fromQueryInstanceId"),
    ResultRequestType("CRC_QRY_getResultDocument_fromResultInstanceId"),
    MasterDeleteRequestType("CRC_QRY_deleteQueryMaster"),
    MasterRenameRequestType("CRC_QRY_renameQueryMaster"),
    GetRequestXml("CRC_QRY_getRequestXml_fromQueryMasterId");
    
    public final Option<String> i2b2RequestType;

    private CRCRequestType(final String i2b2RequestType)
    {
        this.i2b2RequestType = option(i2b2RequestType);
    }
    
    private static Option<String> option(final String s) {
        return Option.apply(s);
    }
}
