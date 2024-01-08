
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.02  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;


public class Ccam5040Func extends FuncEdit {
  String countryCode = "", binCountryCode = "" , ccasLinkType = "FISC";

  public Ccam5040Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      countryCode = wp.itemStr("kk_country_code");     
      binCountryCode = wp.itemStr("kk_bin_country");
    } else {
      countryCode = wp.itemStr("country_code");
      binCountryCode = wp.itemStr("bin_country");
    }
    
    if (isEmpty(countryCode)) {
      errmsg("ISO國家別(2碼): 不可空白");
      return;
    }
    
    if (isEmpty(binCountryCode)) {
        errmsg("ISO國家別(3碼): 不可空白");
        return;
    }        
    
    if (empty(wp.itemStr("country_remark"))) {
      errmsg("國家說明: 不可空白");
      return;
    }
    if (this.isAdd()) {
      return;
    }
    // -other modify-
    sqlWhere = " where country_code=? and bin_country = ?" + " and ccas_link_type=?"+ " and nvl(mod_seqno,0) = ?";
    Object[] parms = new Object[] {countryCode, binCountryCode,ccasLinkType, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("CCA_COUNTRY", sqlWhere,parms)) {
      return;
    }

  }
 

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into CCA_COUNTRY (" + " country_code, " // 1country_code
        + " ccas_link_type, " + " country_remark, " + " rej_code, " + " risk_factor , high_risk , bin_country , country_no ,"
        + " crt_date, crt_user " + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values ("
        + " ?,?,?,?,?,?,?,? " + ",to_char(sysdate,'yyyymmdd'),? " + ",sysdate,?,?,1" + " )";
    Object[] param = new Object[] {countryCode // 1
        , ccasLinkType, wp.itemStr("country_remark"), wp.itemStr("rej_code"), wp.itemNum("risk_factor"),wp.itemNvl("high_risk", "N"),
        binCountryCode,wp.itemStr("country_no"),wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "update CCA_COUNTRY set " + " country_remark =?, " + " rej_code =?, high_risk = ? , risk_factor = ? , country_no = ? ,"
        + " mod_user =?, mod_time=sysdate, mod_pgm =? "
        + ", mod_seqno =nvl(mod_seqno,0)+1 " 
        + " where country_code=? and bin_country = ? and ccas_link_type=? and nvl(mod_seqno,0) = ? "
        ;
    Object[] param = new Object[] {wp.itemStr("country_remark"), wp.itemStr("rej_code"), wp.itemNvl("high_risk", "N") , wp.itemNum("risk_factor") ,
         wp.itemStr("country_no"),wp.loginUser, wp.itemStr("mod_pgm"),countryCode, binCountryCode,ccasLinkType, wp.itemNum("mod_seqno")};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete CCA_COUNTRY " + " where country_code=? and bin_country = ? and ccas_link_type=? and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {countryCode, binCountryCode,ccasLinkType, wp.itemNum("mod_seqno")};
    rc = sqlExec(strSql,param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }

}
