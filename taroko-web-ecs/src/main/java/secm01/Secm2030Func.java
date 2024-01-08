/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  shiyuqi       updated for project coding standard     * 
******************************************************************************/
package secm01;

import busi.FuncAction;

public class Secm2030Func extends FuncAction {
  String wfPgm = "", wfHtml = "";

  @Override
  public void dataCheck() {
    if (ibAdd) {
      wfPgm = wp.itemStr2("kk_wf_pgm");
      wfHtml = wp.itemNvl("kk_wf_html", wfPgm);
    } else {
      wfPgm = wp.itemStr2("wf_pgm");
      wfHtml = wp.itemStr2("wf_html");
    }

    if (empty(wfPgm)) {
      errmsg("程式代碼:不可空白");
      return;
    }
    if (empty(wfHtml)) {
      errmsg("記錄頁面:不可空白");
      return;
    }

    if (ibDelete)
      return;

    if (wp.itemEmpty("wf_column")) {
      errmsg("記錄欄位:不可空白");
      return;
    }

    if (wp.itemEmpty("debit_flag")) {
      errmsg("是否為VD卡:不可空白");
      return;
    }

    // if(wp.item_empty("wf_action")){
    // errmsg("作業類別:不可空白");
    // return ;
    // }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    sql2Insert("sec_idno_data");
    addsqlParm(" ?", ", wf_pgm", wfPgm);
    addsqlParm(",?", ", wf_html", wfHtml);
    addsqlParm(",?", ", wf_column", wp.itemStr2("wf_column"));
    addsqlParm(",?", ", wf_action", wp.colNvl("data_type", "ID"));
    addsqlParm(",?", ", debit_flag", wp.colNvl("debit_flag", "N"));
    addsqlParm(",?", ", crt_user", modUser);
    addsqlYmd(", crt_date");
    addsqlModXXX(modUser, modPgm);

    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      errmsg("insert sec_idno_data error ");
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    sql2Update("sec_idno_data");
    addsqlParm(" wf_column =?", wp.itemStr2("wf_column"));
    addsqlParm(", wf_action =?", wp.colNvl("data_type", "ID"));
    addsqlParm(", debit_flag =?", wp.colNvl("debit_flag", "N"));
    addsqlModXXX(modUser, modPgm);
    sqlWhere("where wf_pgm =?", wfPgm);
    sqlWhere(" and wf_html =?", wfHtml);

    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      errmsg("update sec_idno_data error ");
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " delete sec_idno_data where wf_pgm =? and wf_html =? ";
    setString2(1, wfPgm);
    setString(wfHtml);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete sec_idno_data error ");
    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
