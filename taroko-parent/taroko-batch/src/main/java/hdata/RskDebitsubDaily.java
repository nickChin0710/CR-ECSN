/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
* 109-07-06   V1.00.00  shiyuqi       updated for project coding standard     *
******************************************************************************/
package hdata;

public class RskDebitsubDaily extends BaseBin {

  public String inDate = "";
  public String branchId = "";
  public String debitID = "";
  public String debitIdCode = "";
  public String limitSeqno = "";
  public String subdebitId = "";
  public String debitRelative = "";
  public int pastDay = 0;
  public String blockReason = "";
  public String debitType = "";
  public String idPSeqno = "";
  public String procCode = "";
  public String procDate = "";
  public String blockFlag = "";
  public String sendDate = "";
  public String sendBlockReason = "";
  public String prevBlockReason = "";

  @Override
  public void initData() {

    inDate = "";
    branchId = "";
    debitID = "";
    debitIdCode = "";
    limitSeqno = "";
    subdebitId = "";
    debitRelative = "";
    pastDay = 0;
    blockReason = "";
    debitType = "";
    idPSeqno = "";
    procCode = "";
    procDate = "";
    blockFlag = "";
    sendDate = "";
    sendBlockReason = "";
    prevBlockReason = "";

    rowid = "";
  }

}
