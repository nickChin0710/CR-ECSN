/*
 * 2019-12-09  V1.00.01  Alex  add  dataCheck
 * V1.00.01    yanghan  2020-04-20   修改了變量名稱和方法名稱*
 */
package ccam02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ccam5160Func extends FuncEdit {
  String oppStatus = "";

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
      oppStatus = wp.itemStr("kk_opp_status");
    } else {
      oppStatus = wp.itemStr("opp_status");
    }
    if (empty(oppStatus)) {
      errmsg("停掛原因：不可空白");
      return;
    }

//    if (wp.itemEmpty("ncc_opp_type")) {
//      errmsg("NCCC停掛類別：不可空白");
//      return;
//    }

    if (wp.itemEmpty("opp_remark")) {
      errmsg("說明：不可空白");
      return;
    }

//    if (wp.itemEmpty("neg_opp_reason")) {
//      errmsg("NCCC 停掛原因: 不可空白");
//      return;
//    }

    if (this.isAdd()) {
      return;
    }

    sqlWhere = " where opp_status = ? and nvl(mod_seqno,0) = ? ";
    setString(1, oppStatus);
    setString(2, wp.modSeqno());

    if (this.isOtherModify("CCA_OPP_TYPE_REASON", sqlWhere)) {
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
    strSql = "insert into CCA_OPP_TYPE_REASON (" + " opp_status, " // 1
        + " onus_opp_type, " + " ncc_opp_type, " + " neg_opp_reason, " + " vis_excep_code, "
        + " mst_auth_code, " + " jcb_excp_code, " + " opp_remark, "// 8
        + " jcic_opp_reason , " + " fisc_opp_code , " + " ctrl_code , "
        + " crt_date, crt_user, " + " apr_date, apr_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,?,?,?,?,?,?,?,?,? "
        + ",to_char(sysdate,'yyyymmdd'),? " + ",to_char(sysdate,'yyyymmdd'),? " + ",sysdate,?,?,1"
        + " )";
    Object[] param = new Object[] {oppStatus // 1
        , wp.itemStr("onus_opp_type"), wp.itemStr("ncc_opp_type"), wp.itemStr("neg_opp_reason"),
        wp.itemStr("vis_excep_code"), wp.itemStr("mst_auth_code"), wp.itemStr("jcb_excp_code"),
        wp.itemStr("opp_remark"),wp.itemStr("jcic_opp_reason"),wp.itemStr("fisc_opp_code"),wp.itemStr("ctrl_code"),
        wp.loginUser, wp.itemStr("approval_user"), wp.loginUser,
        wp.itemStr("mod_pgm") // 12
    };
    this.log("kk1=" + oppStatus);
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

    strSql = "update CCA_OPP_TYPE_REASON set " + " onus_opp_type =?, " + " ncc_opp_type =?, "
        + " neg_opp_reason =?, " + " vis_excep_code =?, " + " mst_auth_code =?, "
        + " jcb_excp_code =?, " + " opp_remark =?, "
        + " jcic_opp_reason =?, " + " fisc_opp_code =? , " + " ctrl_code =? , "
        + " mod_user =?, mod_time=sysdate, mod_pgm =? "
        + ", mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;
    Object[] param = new Object[] {wp.itemStr("onus_opp_type"), wp.itemStr("ncc_opp_type"),
        wp.itemStr("neg_opp_reason"), wp.itemStr("vis_excep_code"), wp.itemStr("mst_auth_code"),
        wp.itemStr("jcb_excp_code"), wp.itemStr("opp_remark"),wp.itemStr("jcic_opp_reason"),wp.itemStr("fisc_opp_code"),wp.itemStr("ctrl_code"),
        wp.loginUser, wp.itemStr("mod_pgm"),
        // --sql_where
        oppStatus, wp.modSeqno()};
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
    strSql = "delete CCA_OPP_TYPE_REASON " + sqlWhere;

    setString(1, oppStatus);
    setString(2, wp.modSeqno());

    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

    return rc;
  }

}
