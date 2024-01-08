/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-18  V1.00.00  David FU   program initial                            *
* 106-07-06  V1.00.01  Andy Liu   program update                             *  
* 107-05-31  V1.00.02  Andy Liu   update UI,SQL                              *
* 109-04-28  V1.00.03  YangFang   updated for project coding standard        *
* 109-07-31  V1.00.04  shiyuqi       修改登入帳號及密碼     *
******************************************************************************/

package crdm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Crdm0009Func extends FuncEdit {
  String mKkCardType = "";
  String mKkUnitCode = "";

  public Crdm0009Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    if (this.isDelete() == false) {

      if (wp.itemStr("virtual_flag").equals("Y") || wp.itemStr("ic_kind").equals("B")) {
        if (empty(wp.itemStr("card_item")) == false) {
          errmsg("不可設定卡樣代碼，無法新增/修改");
          return;
        }
      } else {
        if (empty(wp.itemStr("card_item"))) {
          errmsg("卡樣代碼不可空白，無法新增");
          return;
        }
      }
    }

    if (this.ibAdd) {
      mKkUnitCode = wp.itemStr("kk_unit_code");
      mKkCardType = wp.itemStr("kk_card_type");

      if (empty(mKkUnitCode) || empty(mKkCardType)) {
        errmsg("認同集團碼  or 卡種 不可空白，無法新增");
        return;
      }

    } else {
      mKkUnitCode = wp.itemStr("unit_code");
      mKkCardType = wp.itemStr("card_type");
    }
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from crd_item_unit where unit_code=? and card_type = ?";
      Object[] param = new Object[] {mKkUnitCode, mKkCardType};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where unit_code=? and card_type = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkUnitCode, mKkCardType, wp.modSeqno()};
      isOtherModify("crd_item_unit", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into crd_item_unit (" + " unit_code " + ", card_type" + ", card_item"
        + ", virtual_flag" + ", electronic_code" + ", ips_kind" + ", service_code" + ", ic_flag"
        + ", ic_kind" + ", check_key_expire" + ", key_id" + ", deriv_key" + ", l_offln_lmt"
        + ", u_offln_lmt" + ", extn_year" + ", new_extn_mm" + ", reissue_extn_mm" + ", new_vendor"
        + ", mku_vendor" + ", chg_vendor"// 18
        + ", service_id" + ", card_code" + ", crt_date, crt_user " + ", apr_date, apr_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno ,issuer_configuration_id " 
        + " ) values ("
        + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?, ?"
        + ", to_char(sysdate,'yyyymmdd'), ?" + ", to_char(sysdate,'yyyymmdd'), ?"
        + ", sysdate,?,?,1,?" + " )";
    // -set ?value-
    Object[] param = new Object[] {mKkUnitCode // 1
        , mKkCardType, wp.itemStr("card_item"), wp.itemStr("virtual_flag").equals("") ? "N" : "Y",
        wp.itemStr("electronic_code"), wp.itemStr("ips_kind"), wp.itemStr("service_code"),
        wp.itemStr("ic_flag").equals("") ? "N" : "Y", wp.itemStr("ic_kind"),
        wp.itemStr("check_key_expire").equals("") ? "N" : "Y", wp.itemStr("key_id"),
        wp.itemStr("deriv_key"), empty(wp.itemStr("l_offln_lmt")) ? 0 : wp.itemStr("l_offln_lmt"),
        empty(wp.itemStr("u_offln_lmt")) ? 0 : wp.itemStr("u_offln_lmt"),
        empty(wp.itemStr("extn_year")) ? 0 : wp.itemStr("extn_year"),
        empty(wp.itemStr("new_extn_mm")) ? 0 : wp.itemStr("new_extn_mm"),
        empty(wp.itemStr("reissue_extn_mm")) ? 0 : wp.itemStr("reissue_extn_mm"),
        wp.itemStr("new_vendor"), wp.itemStr("mku_vendor"), wp.itemStr("chg_vendor"),
        wp.itemStr("service_id"), wp.itemStr("card_code"), wp.loginUser, wp.itemStr("approval_user"),
        wp.loginUser, wp.itemStr("mod_pgm"),wp.itemStr("issuer_configuration_id")};
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

    strSql =
        "update crd_item_unit set " + "card_item=?" + ", virtual_flag=?" + ", electronic_code=?"
            + ", ips_kind=?" + ", service_code=?" + ", ic_flag=?" + ", ic_kind=?"
            + ", check_key_expire=?" + ", key_id=?" + ", deriv_key=?" + ", l_offln_lmt=?"
            + ", u_offln_lmt=?" + ", extn_year=?" + ", new_extn_mm=?" + ", reissue_extn_mm=?"
            + ", new_vendor=?" + ", mku_vendor=?" + ", chg_vendor=?" + ", service_id=?"
            + ", card_code=?" + " , apr_date=to_char(sysdate,'yyyymmdd'), apr_user=? "
            + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
            + " ,issuer_configuration_id =? "
            + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("card_item"),
        empty(wp.itemStr("virtual_flag")) ? "N" : wp.itemStr("virtual_flag"),
        wp.itemStr("electronic_code"), wp.itemStr("ips_kind"), wp.itemStr("service_code"),
        empty(wp.itemStr("ic_flag")) ? "N" : wp.itemStr("ic_flag"), wp.itemStr("ic_kind"),
        empty(wp.itemStr("check_key_expire")) ? "N" : wp.itemStr("check_key_expire"),
        wp.itemStr("key_id"), wp.itemStr("deriv_key"),
        empty(wp.itemStr("l_offln_lmt")) ? 0 : wp.itemStr("l_offln_lmt"),
        empty(wp.itemStr("u_offln_lmt")) ? 0 : wp.itemStr("u_offln_lmt"),
        empty(wp.itemStr("extn_year")) ? 0 : wp.itemStr("extn_year"),
        empty(wp.itemStr("new_extn_mm")) ? 0 : wp.itemStr("new_extn_mm"),
        empty(wp.itemStr("reissue_extn_mm")) ? 0 : wp.itemStr("reissue_extn_mm"),
        wp.itemStr("new_vendor"), wp.itemStr("mku_vendor"), wp.itemStr("chg_vendor"),
        wp.itemStr("service_id"), wp.itemStr("card_code"), wp.itemStr("approval_user"), wp.loginUser,
        wp.itemStr("mod_pgm"),wp.itemStr("issuer_configuration_id")
        , mKkUnitCode, mKkCardType, wp.modSeqno()};
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
    strSql = "delete crd_item_unit " + sqlWhere;
    Object[] param = new Object[] {mKkUnitCode, mKkCardType, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
