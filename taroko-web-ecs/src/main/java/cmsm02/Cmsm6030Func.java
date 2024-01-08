/*
 * 2019-1219  V1.00.01  Alex  bug fix
 * * 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
 * 
 */
package cmsm02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Cmsm6030Func extends FuncEdit {
  String caseDate = "", caseSeqno = "", procDeptno = "";

  public Cmsm6030Func(TarokoCommon wr) {
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
    caseDate = wp.itemStr("case_date");
    caseSeqno = wp.itemStr("case_seqno");
    procDeptno = wp.itemStr("proc_deptno");

    sqlWhere = " where case_date =:kk1 " + " and case_seqno =:kk2 " + " and proc_deptno =:kk3 "
        + " and mod_seqno =:mod_seqno ";
    setString("kk1", caseDate);
    setString("kk2", caseSeqno);
    setString("kk3", procDeptno);
    item2ParmNum("mod_seqno");
    if (isOtherModify("cms_casedetail", sqlWhere) == true) {
      errmsg("資料已被修改, 請重新讀取");
      return;
    }
  }

  @Override
  public int dbInsert() {
    return 0;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "update cms_casedetail set " + " case_date = :case_date,"
        + " case_seqno = :case_seqno," + " proc_deptno = :proc_deptno," + " proc_id = :proc_id,"
        + " proc_desc = :proc_desc," + " proc_desc2 = :proc_desc2," + " proc_result = :proc_result,"
        // + " finish_date = :finish_date,"
        + " recall_date = :recall_date,"
        // + " case_conf_flag = :case_conf_flag,"
        // + " crt_date = :crt_date,"
        + " crt_user = :crt_user," + " apr_flag = :apr_flag," + " apr_date = :apr_date,"
        + " apr_user = :apr_user," + " mod_user = :mod_user, mod_time=sysdate, mod_pgm =:mod_pgm "
        + ", mod_seqno =nvl(mod_seqno,0)+1 " + " where case_date =:kk1 " + " and case_seqno =:kk2"
        + " and proc_deptno=:kk3" + " and nvl(mod_seqno,0) =:mod_seqno ";;

    item2ParmStr("case_date");
    item2ParmStr("case_seqno");
    item2ParmStr("proc_deptno");
    item2ParmStr("proc_id");
    item2ParmStr("proc_desc");
    item2ParmStr("proc_desc2");
    item2ParmStr("proc_result");
    // item2Parm_ss("finish_date");
    item2ParmStr("recall_date");
    // item2Parm_ss("case_conf_flag");
    // item2Parm_ss("crt_date");
    item2ParmStr("crt_user");
    item2ParmStr("apr_flag");
    item2ParmStr("apr_date");
    item2ParmStr("apr_user");
    item2ParmStr("mod_pgm");
    setString("mod_user", wp.loginUser);
    setString("kk1", caseDate);
    setString("kk2", caseSeqno);
    setString("kk3", procDeptno);
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
    strSql = "delete cms_casedetail " + " where case_date =:kk1 " + " and case_seqno =:kk2"
        + " and proc_deptno=:kk3" + " and nvl(mod_seqno,0) =:mod_seqno ";;
    setString("kk1", caseDate);
    setString("kk2", caseSeqno);
    setString("kk3", procDeptno);
    item2ParmNum("mod_seqno");

    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}

