/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
* 109-07-06   V1.00.00  shiyuqi       updated for project coding standard     *
******************************************************************************/
package hdata;

public class VocAppc extends BaseBin {

  public String assignKind = "";
  public String txDate = "";
  public int txSeq = 0;
  public String cardNo = "";
  public String acctID = "";
  public String acctNo = "";
  public String effcMonth = "";
  public double txAmt = 0;
  public String txStatus = "";
  public String sendFlag = "";
  public String procSeqno = "";
  public String crtDate = "";
  public String crtTime = "";
  public String rtnCode = "";
  public String rtnDate = "";
  public String diffCode = "";
  public double rtnAmt = 0;
  public String rtnTime = "";
  public String rtnAcctNo = "";
  public String rtnId = "";

  @Override
  public void initData() {
    assignKind = "";
    txDate = "";
    txSeq = 0;
    cardNo = "";
    acctID = "";
    acctNo = "";
    effcMonth = "";
    txAmt = 0;
    txStatus = "";
    sendFlag = "";
    procSeqno = "";
    crtDate = "";
    crtTime = "";
    rtnCode = "";
    rtnDate = "";
    diffCode = "";
    rtnAmt = 0;
    rtnTime = "";
    rtnAcctNo = "";
    rtnId = "";
    rowid = "";
  }

}
