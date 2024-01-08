
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00  yanghan  修改了變量名稱和方法名稱*
******************************************************************************/
package ccam02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Ccam5010Func extends FuncEdit {
  String sysId = "RISK", riskType;

  public Ccam5010Func(TarokoCommon wr) {
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
      riskType = wp.itemStr("kk_risk_type");
    } else {
      riskType = wp.itemStr("risk_type");
    }

    if (isEmpty(riskType)) {
      errmsg("風險類別 不可空白");
      return;
    }
    if (empty(wp.itemStr("risk_desc"))) {
      errmsg("風險說明 不可空白");
      return;
    }

    if (this.isAdd()) {
      return;
    }

    if (ibDelete) {
      if (checkRiskDetl() == false) {
        errmsg("請先至「風險別消費限額參數維護」刪除明細後才可刪除風險類別");
        return;
      }
    }


    sqlWhere = " where sys_id=?" + " and sys_key =?" + " and nvl(mod_seqno,0) =?";
    Object[] parms = new Object[] {sysId, riskType, wp.itemNum("mod_seqno")};
    if (this.isOtherModify("cca_sys_parm1", sqlWhere, parms)) {
      wp.log(sqlWhere, parms);
      return;
    }
  }

  boolean checkRiskDetl() {

    String sql1 = " select count(*) as db_cnt1 from cca_risk_consume_parm where risk_type = ? ";
    sqlSelect(sql1, new Object[] {riskType});

    if (colNum("db_cnt1") > 0)
      return false;

    String sql2 = " select count(*) as db_cnt2 from cca_risk_consume_parm_t where risk_type = ? ";
    sqlSelect(sql2, new Object[] {riskType});

    if (colNum("db_cnt2") > 0)
      return false;

    return true;
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    // is_sql = "insert into CCA_sys_parm1 ("
    // + " sys_id, " // 1
    // + " sys_key, "
    // + " sys_data1, "
    // + " crt_date, crt_user "
    // + ", mod_time, mod_user, mod_pgm, mod_seqno"
    // + " ) values ("
    // + " ?,?,? "
    // + ",to_char(sysdate,'yyyymmdd'),? "
    // + ",sysdate,?,?,1"
    // + " )";
    // // -set ?value-
    // Object[] param = new Object[] {
    // kk1 // 1
    // ,
    // kk2,
    // wp.item_ss("risk_desc"),
    // wp.loginUser,
    // wp.loginUser,
    // wp.item_ss("mod_pgm")
    // };
    strSql = "insert into CCA_sys_parm1 (" + " sys_id, " // 1
        + " sys_key, " + " sys_data1, " + " sys_data3, " + " crt_date, crt_user , apr_date , apr_user "
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values ("
        + " :sys_id, :sys_idkey, :sys_data1, :sys_data3 "
        + ",to_char(sysdate,'yyyymmdd'),:crt_user , to_char(sysdate,'yyyymmdd') , :apr_user "
        + ",sysdate, :mod_user, :mod_pgm, 1" + " )";
    // -set ?value-
    try {
      this.setString("sys_id", sysId);
      setString("sys_idkey", riskType);
      item2ParmStr("sys_data1", "risk_desc");
      item2ParmStr("sys_data3","high_risk_flag");
      setString("crt_user", wp.loginUser);
      setString("apr_user", wp.itemStr("approval_user"));
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.modPgm());
    } catch (Exception ex) {
      wp.log("dbInsert", ex);
    }
    sqlExec(strSql);
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

    strSql = "update CCA_sys_parm1 set " + " sys_data1 = :sys_data1, "
    	+ " sys_data3 = :sys_data3 ,"
        + " mod_user = :mod_user, mod_time=sysdate, mod_pgm =:mod_pgm "
        + ", mod_seqno =nvl(mod_seqno,0)+1 " + ", apr_date = to_char(sysdate,'yyyymmdd') "
        + ", apr_user = :apr_user  " + " where sys_id =:kk " + " and sys_key =:kk2"
        + " and nvl(mod_seqno,0) =:mod_seqno ";;
    // Object[] param = new Object[] {
    // wp.item_ss("risk_desc"),
    // wp.item_ss("mod_user"),
    // wp.item_ss("mod_pgm")
    // };
    item2ParmStr("sys_data1", "risk_desc");
    item2ParmStr("sys_data3", "high_risk_flag");
    setString("mod_user", wp.loginUser);
    item2ParmStr("mod_pgm");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("kk", sysId);
    setString("kk2", riskType);
    item2ParmNum("mod_seqno");

    rc = sqlExec(strSql);
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
    strSql = "delete CCA_sys_parm1 " + " where sys_id =:kk1 " + " and sys_key =:kk2"
        + " and nvl(mod_seqno,0) =:mod_seqno ";
    // ddd("del-sql="+is_sql);
    setString("kk1", sysId);
    setString("kk2", riskType);
    item2ParmNum("mod_seqno");
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
