/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-20  V1.00.01  Zuwei       code format                              *
*                                                                            *  
******************************************************************************/
package busi.func;

import busi.FuncBase;

public class CcaSpecialVisa extends FuncBase {

  public String blockReason = "";
  public String negDelDate = "";
  public String vmjReason = "";
  public String negReason = "";
  public String vmjRespCode = "";
  public String negRespCode = "";
  public String cardNo = "";
  public double mastVipAmt = 0;
  public String specRemark = "";
  public String binType = "";

  // public String vmj_del_resp="";
  // public String neg_del_resp="";

  public CcaSpecialVisa(taroko.com.TarokoCommon wr) {
    this.wp = wr;
    this.conn = wp.getConn();
    modUser = wp.loginUser;
    modPgm = wp.modPgm();
  }

  public int dbUpdate() {
    strSql =
        "update cca_special_visa set" + " from_type ='1'" + ", spec_status =:spec_status"
            + ", spec_del_date =:del_date" + ", spec_mst_vip_amt =:mst_vip_amt"
            + ", spec_outgo_reason =:vmj_reason" + ", spec_neg_reason =:neg_reason"
            + ", vm_resp_code =:vmj_resp_code" + ", neg_resp_code =:neg_resp_code"
            + ", spec_remark =:spec_remark" + ", logic_del_date =''" + ", logic_del_time =''"
            + ", spec_del_user =''" + ", vm_del_resp_code =''" + ", neg_del_resp_code =''"
            + ", logic_del ='N'" + ", chg_date =" + commSqlStr.sysYYmd + ", chg_time =" + commSqlStr.sysTime
            + ", chg_user =:chg_user" + "," + commSqlStr.setModxxx(modUser, modPgm)
            + " where card_no =:kk1";

    setString2("spec_status", blockReason);
    setString2("del_date", negDelDate);
    setDouble2("mst_vip_amt", mastVipAmt);
    setString2("vmj_reason", vmjReason);
    setString2("neg_reason", negReason);
    setString2("vmj_resp_code", vmjRespCode);
    setString2("neg_resp_code", negRespCode);
    setString2("spec_remark", specRemark);
    setString2("chg_user", modUser);
    setString2("kk1", cardNo);

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      sqlErr("cca_special_visa.update, kk=" + cardNo);
      return -1;
    }

    // -insert-
    if (sqlRowNum == 0) {
      return dbInsert();
    }

    dataClear();
    return 1;
  }

  public int dbInsert() {
    busi.SqlPrepare sp = new busi.SqlPrepare();
    sp.sql2Insert("cca_special_visa");

    sp.ppstr2("card_no", cardNo);
    sp.ppstr2("bin_type", binType);
    sp.ppstr2("from_type", "1");
    sp.ppstr2("spec_status", blockReason);
    sp.ppstr2("spec_del_date", negDelDate);
    sp.ppdouble("spec_mst_vip_amt", mastVipAmt);
    sp.ppstr2("spec_outgo_reason", vmjReason);
    sp.ppstr2("spec_neg_reason", negReason);
    sp.ppstr2("vm_resp_code", vmjRespCode);
    sp.ppstr2("neg_resp_code", negRespCode);
    // sp.ppp("fisc_resp_code
    sp.ppstr2("spec_remark", wp.itemStr2("spec_remark"));
    // sp.ppp("logic_del_date
    // sp.ppp("logic_del_time
    // sp.ppp("spec_del_user
    // sp.ppp("vm_del_resp_code
    // sp.ppp("neg_del_resp_code
    // sp.ppp("fisc_del_resp_code
    sp.ppstr2("logic_del", "N");
    // sp.ppp("mcas_resp_code
    sp.ppymd("crt_date");
    sp.pptime("crt_time");
    sp.ppstr2("crt_user", modUser);
    // sp.ppp("chg_date
    // sp.ppp("chg_time
    // sp.ppp("chg_user
    sp.modxxx(modUser, modPgm);

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum != 1) {
      sqlErr("cca_special_visa.insert, kk=" + cardNo);
      return -1;
    }

    dataClear();
    return 1;
  }

  public int dbDelete() {
    busi.SqlPrepare sp = new busi.SqlPrepare();

    sp.sql2Update("cca_special_visa");
    sp.addsql("from_type ='1'");
    sp.ppymd("logic_del_date");
    sp.pptime("logic_del_time");
    sp.ppstr2("spec_del_user", modUser);
    sp.ppstr2("vm_del_resp_code", vmjRespCode);
    sp.ppstr2("neg_del_resp_code", negRespCode);
    sp.ppstr2("logic_del", "Y");
    sp.ppymd("chg_date");
    sp.pptime("chg_time");
    sp.ppstr2("chg_user", modUser);
    sp.modxxx(modUser, modPgm);
    sp.sql2Where(" where card_no =?", cardNo);

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum < 0) {
      sqlErr("cca_special_visa.logic-del, kk=" + cardNo);
      return -1;
    }

    dataClear();
    return 1;
  }

  public void dataClear() {
    blockReason = "";
    negDelDate = "";
    vmjReason = "";
    negReason = "";
    vmjRespCode = "";
    negRespCode = "";
    cardNo = "";
    mastVipAmt = 0;
    specRemark = "";
    binType = "";
    // vmj_del_resp="";
    // neg_del_resp="";
  }

}
