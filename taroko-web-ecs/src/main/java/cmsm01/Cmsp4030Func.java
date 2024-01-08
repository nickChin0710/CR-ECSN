/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.01  shiyuqi       updated for project coding standard     *  
* 109-12-25  V1.0002   Justin         parameterize sql
******************************************************************************/
package cmsm01;

import busi.FuncAction;

public class Cmsp4030Func extends FuncAction {
  String isBatchNo = "", lsYmd = "";
  int liNo = 0;

  @Override
  public void dataCheck() {
    isBatchNo = varsStr("batch_no");
    lsYmd = this.getSysDate();
    if (empty(isBatchNo)) {
      String sql1 = "select substr(max(appr_no),9,2) as ls_batch_no " + " from cms_acaj "
          + " where appr_no like ? ";
      sqlSelect(sql1, new Object[] {lsYmd + "%"});
      if (this.eqIgno(colStr("ls_batch_no"), null) || empty(colStr("ls_batch_no"))) {
        isBatchNo = lsYmd + "01";
      } else {
        liNo = (int) (colNum("ls_batch_no") + 1);
        if (liNo < 10) {
          isBatchNo = lsYmd + "0" + liNo;
        } else if (liNo >= 10) {
          isBatchNo = lsYmd + liNo;
        }
      }
    }

    if (eqIgno(colStr("acct_post_flag"), "Y")) {
      errmsg("此筆資料已匯入不可放行或取消");
      return;
    }

  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int doProc() {
    msgOK();
    selectData();
    if (rc != 1)
      return -1;
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = " update cms_acaj set " + " apr_user =:apr_user ,"
        + " apr_date = to_char(sysdate,'yyyymmdd') ," + " appr_no =:appr_no ,"
        + " mod_user =:mod_user ," + " mod_time =sysdate ," + " mod_pgm ='cmsp4030' ,"
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where hex(rowid) =:rowid ";
    setString("appr_no", isBatchNo);
    var2ParmStr("rowid");
    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);

    this.sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("Update cms_acaj error, " + getMsg());
      rc = -1;
    }

    return rc;
  }

  public int delProc() {
    msgOK();
    selectData();
    if (rc != 1)
      return -1;
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = " update cms_acaj set " + " apr_user ='' ," + " apr_date ='' ," + " appr_no ='' ,"
        + " mod_user =:mod_user ," + " mod_time =sysdate ," + " mod_pgm ='cmsp4030' ,"
        + " mod_seqno =nvl(mod_seqno,0)+1 " + " where hex(rowid) =:rowid ";
    var2ParmStr("rowid");
    setString("mod_user", wp.loginUser);

    this.sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("Update cms_acaj error, " + getMsg());
      rc = -1;
    }

    return rc;
  }

  void selectData() {
    strSql = "select acct_post_flag " + " from cms_acaj " + " where 1=1 and hex(rowid) = ? ";
    sqlSelect(strSql, new Object[] {varsStr("rowid")});
    if (sqlRowNum <= 0) {
      errmsg("D檔調整資料, 已不存在");
    }
  }

}
