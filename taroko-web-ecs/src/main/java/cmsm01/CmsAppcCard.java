/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.01  shiyuqi       updated for project coding standard     *  
******************************************************************************/
package cmsm01;

import busi.FuncBase;

public class CmsAppcCard extends FuncBase {
  public String apcTnscode = ""; // 交易代號
  public String apcFunc = ""; // 功能別
  public String apcIdno = ""; // ID或統編
  public String apcRcvcode = ""; // 回覆碼
  public String apcRcvdesc = ""; // 回覆說明
  public String apcAtmcardno = ""; // ATM卡號
  public String apcAcctno = ""; // 帳號
  public String apcDesc1 = ""; // 申請狀態 */ --
  public String apcDesc2 = ""; // 使用註記 */ --
  public String apcData01 = ""; // 資料 */ --
  public String apcLostcode = ""; // 掛失回應代碼 */ --
  public String apcLostdesc = ""; // 掛失說明 */ --
  public double apcAcctamt = 0; // 金額 */ --
  public String apcLostdate = ""; // 掛失日期 */ --
  public String apcLosttime = ""; // 掛失時間 */ --
  public String apcDesc3 = ""; // 說明代碼三 */ --
  public String apcCardType = ""; // 卡別 */ --
  public String apcSubType = ""; // 卡別二 */ --


  public int insertData() {
    strSql = "insert into cms_appc_card (" + "mod_date, mod_time, mod_user, mod_pgm"
        + ", apc_tnscode" + ", apc_func" + ", apc_idno" + ", apc_rcvcode" + ", apc_rcvdesc"
        + ", apc_atmcardno" + ", apc_acctno" + ", apc_desc1" + ", apc_desc2" + ", apc_data01"
        + ", apc_lostcode" + ", apc_lostdesc" + ", apc_acctamt" + ", apc_lostdate"
        + ", apc_losttime" + ", apc_desc3" + ", apc_card_type" + ", apc_sub_type " + " ) values ("
        + commSqlStr.sysYYmd + "," + commSqlStr.sysTime + ",:mod_user,:mod_pgm" + ", :apc_tnscode"
        + ", :apc_func" + ", :apc_idno" + ", :apc_rcvcode" + ", :apc_rcvdesc" + ", :apc_atmcardno"
        + ", :apc_acctno" + ", :apc_desc1" + ", :apc_desc2" + ", :apc_data01" + ", :apc_lostcode"
        + ", :apc_lostdesc" + ", :apc_acctamt" + ", :apc_lostdate" + ", :apc_losttime"
        + ", :apc_desc3" + ", :apc_card_type" + ", :apc_sub_type " + " )";
    setString2("mod_user", modUser);
    setString2("mod_pgm", modPgm);
    setString2("apc_tnscode  ", apcTnscode);
    setString2("apc_func     ", apcFunc);
    setString2("apc_idno     ", apcIdno);
    setString2("apc_rcvcode  ", apcRcvcode);
    setString2("apc_rcvdesc  ", apcRcvdesc);
    setString2("apc_atmcardno", apcAtmcardno);
    setString2("apc_acctno   ", apcAcctno);
    setString2("apc_desc1    ", apcDesc1);
    setString2("apc_desc2    ", apcDesc2);
    setString2("apc_data01   ", apcData01);
    setString2("apc_lostcode ", apcLostcode);
    setString2("apc_lostdesc ", apcLostdesc);
    setDouble2("apc_acctamt  ", apcAcctamt);
    setString2("apc_lostdate ", apcLostdate);
    setString2("apc_losttime ", apcLosttime);
    setString2("apc_desc3    ", apcDesc3);
    setString2("apc_card_type", apcCardType);
    setString2("apc_sub_type ", apcSubType);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert cms_appc_card error, " + sqlErrtext);
    }

    return rc;
  }

  public void dataClear() {
    apcTnscode = "";
    apcFunc = "";
    apcIdno = "";
    apcRcvcode = "";
    apcRcvdesc = "";
    apcAtmcardno = "";
    apcAcctno = "";
    apcDesc1 = "";
    apcDesc2 = "";
    apcData01 = "";
    apcLostcode = "";
    apcLostdesc = "";
    apcAcctamt = 0;
    apcLostdate = "";
    apcLosttime = "";
    apcDesc3 = "";
    apcCardType = "";
    apcSubType = "";
  }


}
