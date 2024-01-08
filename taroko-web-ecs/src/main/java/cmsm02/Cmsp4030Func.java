package cmsm02;
/** cms02;客服D檔調整主管覆核作業
 * 2019-0614:  JH    p_xxx >>acno_pxxx
 * 2018-0726:	JH		modify
* 109-04-27 shiyuqi       updated for project coding standard     *  
 * */

import busi.FuncAction;

public class Cmsp4030Func extends FuncAction {
  String isBatchNo = "", lsYmd = "";
  int liNo = 0;

  @Override
  public void dataCheck() {
    selectCmsAcaj(varsStr("rowid"));
    if (rc != 1)
      return;

    if (eqIgno(colStr("acct_post_flag"), "Y")) {
      errmsg("此筆資料已匯入不可放行或取消");
      return;
    }

    if (colEmpty("apr_date") == false) {
      rc = 0;
      return;
    }

    if (empty(varsStr("batch_no")) && empty(isBatchNo))
      isBatchNo = getApprNo();
  }

  void cancelCheck() {
    selectCmsAcaj(varsStr("rowid"));
    if (rc != 1)
      return;
    if (eqIgno(colStr("acct_post_flag"), "Y")) {
      errmsg("此筆資料已匯入不可放行或取消");
      return;
    }

    if (colEmpty("apr_date")) {
      rc = 0;
      return;
    }
  }

  String getApprNo() {
    String lsBatchNo = varsStr("batch_no");
    if (!empty(lsBatchNo))
      return lsBatchNo;

    lsYmd = this.getSysDate();
    String sql1 = "select substr(max(appr_no),9,2) as ls_batch_no " + " from cms_acaj "
        + " where appr_no like ? ";
    sqlSelect(sql1, new Object[] {lsYmd + "%"});
    if (sqlRowNum <= 0) {
      return lsYmd + "01";
    }

    liNo = colInt("ls_batch_no") + 1;
    if (liNo < 10) {
      return lsYmd + "0" + liNo;
    }
    return lsYmd + liNo;
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
    msgOK();
    this.actionCode = "C1";
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = " update cms_acaj set " + " apr_user =:apr_user ,"
        + " apr_date = to_char(sysdate,'yyyymmdd') ," + " appr_no =:appr_no ,"
        + commSqlStr.setModxxx(modUser, modPgm) + " where rowid =:rowid ";
    setString2("apr_user", modUser);
    setString2("appr_no", isBatchNo);
    // kk
    setRowId2("rowid", varsStr("rowid"));

    this.sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("Update cms_acaj error, " + getMsg());
    }

    return rc;
  }

  public int cancelProc() {
    msgOK();
    this.actionCode = "C2";
    cancelCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = " update cms_acaj set " + " apr_user ='' ," + " apr_date ='' ," + " appr_no ='' ,"
        + commSqlStr.setModxxx(modUser, modPgm) + " where rowid =? ";

    setRowId2(1, varsStr("rowid"));
    this.sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("[cancel]Update cms_acaj error");
    }

    return rc;
  }

  void selectCmsAcaj(String aRowid) {
    strSql = "select acct_post_flag, apr_date " + " from cms_acaj " + " where rowid =?";

    setRowId2(1, aRowid);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("D檔調整資料, 已不存在");
    }
  }

}
