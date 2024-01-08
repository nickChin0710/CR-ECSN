/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
* 109-07-06   V1.00.00  shiyuqi       updated for project coding standard     *
******************************************************************************/
package hdata;

public class RskDebitbadDaily extends BaseBin {

  public String inDate = "";
  public String branchId = "";
  public String debitId = "";
  public String debitIdCode = "";
  public String limitSeqno = "";
  public String creditAcctno = "";
  public String acctStatus = "";
  public String chiName = "";
  public String birthday = "";
  public String currCode = "";
  public double creditAmt = 0;
  public int pastDay = 0;
  public String nolistFlag = "";
  public String abnormalFlag = "";
  public String blockReason = "";
  public String debitType = "";
  public String idPSeqno = "";
  public String procCode = "";
  public String procDate = "";
  public String userId = "";
  public String sendDate = "";
  public String sendBlockReason = "";
  public String blockFlag = "";
  public String prevBlockReason = "";

  @Override
  public void initData() {

    inDate = "";
    branchId = "";
    debitId = "";
    debitIdCode = "";
    limitSeqno = "";
    creditAcctno = "";
    acctStatus = "";
    chiName = "";
    birthday = "";
    currCode = "";
    creditAmt = 0;
    pastDay = 0;
    nolistFlag = "";
    abnormalFlag = "";
    blockReason = "";
    debitType = "";
    idPSeqno = "";
    procCode = "";
    procDate = "";
    userId = "";
    sendDate = "";
    sendBlockReason = "";
    blockFlag = "";
    prevBlockReason = "";

    rowid = "";
  }

}
