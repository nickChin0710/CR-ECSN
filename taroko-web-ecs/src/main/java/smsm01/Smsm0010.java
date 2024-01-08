/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-29  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-30  V1.00.01  shiyuqi       修改无意义命名                                                                                     *
******************************************************************************/
package smsm01;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Smsm0010 extends BaseAction {

  String msgPgm = "";

  @Override
  public void userAction() throws Exception {
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 動態查詢 */
      querySelect2();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      if(wp.itemEq("ax_proc_code","1"))	wfAjaxKey();
      else	ajaxFunc();
      return;
    } else if (eqIgno(wp.buttonCode, "D2")) {
      // -刪除-明細資料-
      strAction = "U2";
      detl2Delete();
    } 
//    else if (eqIgno(wp.buttonCode, "Q2")) {
//      strAction = "Q2";
//      wfAjaxKey();
//      return ;
//    }


  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "smsm0010")) {
        wp.optionKey = wp.colStr(0, "ex_msg_pgm");
        dddwList("dddw_msg_pgm", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='SMS_MSG_PGM'");

        wp.optionKey = wp.colStr(0, "ex_msg_dept");
        dddwList("dddw_dept_code", "ptr_dept_code", "dept_code", "dept_name", "where 1=1");
      }

      if (eqIgno(wp.respHtml, "smsm0010_detl")) {
        wp.optionKey = wp.colStr(0, "kk_msg_pgm");
        dddwList("dddw_msg_pgm", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='SMS_MSG_PGM'");

        wp.optionKey = wp.colStr(0, "msg_dept");
        dddwList("dddw_dept_code", "ptr_dept_code", "dept_code", "dept_name", "where 1=1");

        wp.optionKey = wp.colStr(0, "msg_id");
        dddwList("dddw_msg_id", "sms_msg_content", "msg_id", "msg_desc", "where 1=1");
      }

      if (eqIgno(wp.respHtml, "smsm0010_actp")) {
        wp.optionKey = wp.colStr(0, "ex_data_code");
        dddwList("dddw_accttype", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
      }

    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {

    getWhere();
    wp.setQueryMode();

    queryRead();
  }

  void getWhere() {
	sqlParm.clear();
    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_msg_pgm"), "msg_pgm")
        + sqlCol(wp.itemStr("ex_msg_id"), "msg_id") + sqlCol(wp.itemStr("ex_msg_dept"), "msg_dept");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " msg_pgm , msg_dept , msg_serve , msg_id , msg_desc , "
        + " (select dept_name from ptr_dept_code where dept_code = msg_dept) as tt_msg_dept ";
    wp.daoTable = " sms_msg_id ";
    wp.whereOrder = " order by msg_pgm Asc ";
    getWhere();
    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setPageValue();
    wp.setListCount(0);

  }

  @Override
  public void querySelect() throws Exception {
    msgPgm = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(msgPgm))
      msgPgm = itemkk("msg_pgm");
    if (empty(msgPgm)) {
      alertErr2("使用程式不可空白");
      return;
    }

    wp.selectSQL = " msg_pgm , " + " msg_dept , " + " msg_userid , " + " msg_serve , "
        + " msg_run_day , " + " msg_hour1 , " + " msg_hour2 , " + " msg_id , "
        + " substring(msg_desc,1,75) as msg_desc1 , " + " substring(msg_desc,76,75) as msg_desc2 , "
        + " substring(msg_desc,151,75) as msg_desc3 , "
        + " substring(msg_desc,226,75) as msg_desc4 , " + " msg_send_flag , " + " send_eff_date1 , "
        + " send_eff_date2 , " + " msg_sel_amt01 , " + " acct_type_sel , " + " msg_amt01 , "
        + " crt_date , " + " crt_user , " + " mod_user , "
        + " to_char(mod_time,'yyyymmdd') as mod_date , " + " hex(rowid) as rowid , "
        + " mod_seqno ";

    wp.daoTable = " sms_msg_id ";
    wp.whereStr = " where 1=1 " + sqlCol(msgPgm, "msg_pgm");
    pageSelect();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

  }

  void querySelect2() throws Exception {
    msgPgm = wp.itemStr("data_k1");
    dataRead2();
  }

  void dataRead2() throws Exception {
    if (empty(msgPgm))
      msgPgm = wp.itemStr("msg_pgm");
    if (empty(msgPgm)) {
      alertErr2("使用程式不可空白");
      return;
    }

    wp.selectSQL =
        " data_code , (select chin_name from ptr_acct_type where data_code = acct_type) as tt_data_code ";
    wp.daoTable = " sms_dtl_data ";
    wp.whereStr = " where 1=1 " + " and table_name = 'SMS_MSG_ID' and data_type ='1' "
        + sqlCol(msgPgm, "data_key");

    pageQuery();
    if (sqlNotFind()) {
      selectOK();
      return;
    }

    wp.setListCount(0);

  }

  @Override
  public void saveFunc() throws Exception {

    if (checkApproveZz() == false)
      return;

    smsm01.Smsm0010Func func = new smsm01.Smsm0010Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
    } else
      saveAfter(false);

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    btnModeAud();

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  public void wfAjaxKey() throws Exception {    
    // String ls_winid =
    selectData(wp.itemStr("ax_mag_id"));
    if (rc != 1) {
      wp.addJSON("msg_desc1", "");
      wp.addJSON("msg_desc2", "");
      wp.addJSON("msg_desc3", "");
      wp.addJSON("msg_desc4", "");
      return;
    }
    wp.addJSON("msg_desc1", sqlStr("msg_desc1"));
    wp.addJSON("msg_desc2", sqlStr("msg_desc2"));
    wp.addJSON("msg_desc3", sqlStr("msg_desc3"));
    wp.addJSON("msg_desc4", sqlStr("msg_desc4"));

  }

  void selectData(String msgId) {
    String sql1 = " select " + " substring(msg_content,1,75) as msg_desc1 , "
        + " substring(msg_content,76,75) as msg_desc2 , "
        + " substring(msg_content,151,75) as msg_desc3 , "
        + " substring(msg_content,226,75) as msg_desc4  " + " from sms_msg_content "
        + " where msg_id = ? ";

    sqlSelect(sql1, new Object[] {msgId});

    if (sqlRowNum <= 0) {
      alertErr2("簡訊代號不存在:" + msgId);
      return;
    }

  }

  void ajaxFunc() throws Exception {

    // -insert detl-
    smsm01.Smsm0010Func func = new smsm01.Smsm0010Func();
    func.setConn(wp);

    rc = func.dbInsertDetl();
    sqlCommit(rc);

    if (rc != 1) {
      wp.addJSON("ex_errmsg", func.getMsg());
      // err_alert(func.getMsg());
    } else {
      wp.addJSON("ax_errmsg", "");
    }
  }

  void detl2Delete() throws Exception {
    smsm01.Smsm0010Func func = new smsm01.Smsm0010Func();
    func.setConn(wp);
    int ilCnt = 0, ilOk = 0, ilErr = 0;

    String[] aaOpt = wp.itemBuff("opt");
    String[] lsDataCode = wp.itemBuff("data_code");
    wp.listCount[0] = wp.itemRows("data_code");
    func.varsSet("data_key", wp.itemStr2("msg_pgm"));
    for (int ii = 0; ii < wp.itemRows("data_code"); ii++) {
      if (checkBoxOptOn(ii, aaOpt) == false)
        continue;
      ilCnt++;
      func.varsSet("data_code", lsDataCode[ii]);
      if (func.dbDeleteDetl() != 1) {
        ilErr++;
        wp.colSet(ii, "ok_flag", "X");
        dbRollback();
        continue;
      } else {
        ilOk++;
        wp.colSet(ii, "ok_flag", "V");
        sqlCommit(1);
        continue;
      }
    }

    if (ilCnt == 0) {
      alertErr2("請選擇要刪除的資料");
      return;
    }

    alertMsg("刪除明細完成,成功:" + ilOk + " 失敗:" + ilErr);

  }

}
