
/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
* 109-07-06   V1.00.00  shiyuqi       updated for project coding standard     *
******************************************************************************/package hdata;
/* V.2018-0619.JH
 * 
 * */

public class SmsMsgDtl extends BaseBin {
  public String msgSeqno = "";
  public String msgDept = "";
  public String msgUserid = "";
  public String msgPgm = "";
  public String idPSeqno = "";
  public String pSeqno = "";
  public String idNo = "";
  public String acctType = "";
  public String cardNo = "";
  public String msgID = "";
  public String cellarPhone = "";
  public String cellphoneCheckFlag = "";
  public String chiName = "";
  public String exId = "";
  public String msgDesc = "";
  public double minPay = 0;
  public String addMode = "";
  public String resendFlag = "";
  public String sendFlag = "";
  public String priorFlag = "";
  public String createTxtDate = "";
  public String createTxtTime = "";
  public String chiNameFlag = "";
  public String procFlag = "";
  public String sms24Flag = "";
  public String crtDate = "";
  public String crtUser = "";
  public String aprDate = "";
  public String aprUser = "";
  public String aprFlag = "";


  @Override
  public void initData() {
    msgSeqno = "";
    msgDept = "";
    msgUserid = "";
    msgPgm = "";
    idPSeqno = "";
    pSeqno = "";
    idNo = "";
    acctType = "";
    cardNo = "";
    msgID = "";
    cellarPhone = "";
    cellphoneCheckFlag = "";
    chiName = "";
    exId = "";
    msgDesc = "";
    minPay = 0;
    addMode = "";
    resendFlag = "";
    sendFlag = "";
    priorFlag = "";
    createTxtDate = "";
    createTxtTime = "";
    chiNameFlag = "";
    procFlag = "";
    sms24Flag = "";
    crtDate = "";
    crtUser = "";
    aprDate = "";
    aprUser = "";
    aprFlag = "";

    rowid = "";
  }

}
