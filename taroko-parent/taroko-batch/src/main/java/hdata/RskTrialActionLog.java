/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
* 109-07-06   V1.00.00  shiyuqi       updated for project coding standard     *
******************************************************************************/
package hdata;

public class RskTrialActionLog extends BaseBin {

  public String batchNo = "";
  public String idPSeqno = "";
  public String pSeqno = "";
  public String actionCode = "";
  public String actionDate = "";
  public String closeFlag = "";
  public String closeDate = "";
  public double creditLimitBef = 0;
  public double creditLimitAft = 0;
  public String blockReason4 = "";
  public String acctType = "";
  public String msgFlag = "";
  public String blockReason5 = "";
  public int cardCurrCnt = 0;
  public int supCurrCnt = 0;
  public String blockReasonBef = "";
  public String blockReasonAft = "";
  public String specStatus = "";

  @Override
  public void initData() {
    batchNo = "";
    idPSeqno = "";
    pSeqno = "";
    actionCode = "";
    actionDate = "";
    closeFlag = "";
    closeDate = "";
    creditLimitBef = 0;
    creditLimitAft = 0;
    blockReason4 = "";
    acctType = "";
    msgFlag = "";
    blockReason5 = "";
    cardCurrCnt = 0;
    supCurrCnt = 0;
    blockReasonBef = "";
    blockReasonAft = "";
    specStatus = "";
    rowid = "";
  }

}
