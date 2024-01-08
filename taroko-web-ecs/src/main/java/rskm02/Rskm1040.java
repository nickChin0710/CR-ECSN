/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-06-19  V1.00.01   JH                  8.原額用卡, 戶特指=81
* 109-01-06  V1.00.02   Justin Wu    updated for archit.  change
* 109-04-27  V1.00.03  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
* *  110/1/4  V1.00.04  yanghan       修改了變量名稱和方法名稱            *
******************************************************************************/
package rskm02;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Rskm1040 extends BaseAction {

  String batchNo = "", riskGroup = "";

  @Override
  public void userAction() throws Exception {
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // * 查詢功能 */
        strAction = "R";
        dataRead();
        break;
      case "A":
        /* 新增功能 */
        saveFunc();
        break;
      case "U":
        strAction = "U";
        saveFunc();
        break;
      case "D":
        /* 刪除功能 */
        saveFunc();
        break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "C":
        // -資料處理-
        procFunc();
        break;
      case "AJAX":
        // AJAX 20200106 updated for archit. change
        wfAjaxKey();
        break;
      default:
        break;
    }
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "rskm1040")) {
        wp.optionKey = wp.colStr("ex_batch_no");
        dddwList("dddw_batch_no", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='TRIAL_ACTION_BATCH'");
      }

    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "rskm1040_detl")) {
        wp.optionKey = wp.colStr("kk_batch_no");
        dddwList("dddw_kk_batch_no", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type='TRIAL_ACTION_BATCH'");
      }

    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "rskm1040_detl")) {
        wp.optionKey = wp.colStr("kk_risk_group");
        dddwList("dddw_risk_group", "rsk_trial_parm2", "risk_group", "risk_group_desc",
            "where 1=1");
      }

    } catch (Exception ex) {
    }


    try {
      if (eqIgno(wp.respHtml, "rskm1040_detl")) {
        wp.optionKey = wp.colStr("spec_status");
        dddwList("dddw_spec_status", "cca_spec_code", "spec_code", "spec_desc",
            "where spec_type ='2'");
      }

    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "rskm1040_detl")) {
        wp.optionKey = wp.colStr("action_code");
        ddlbList("dddw_action_code", wp.colStr("action_code"), "ecsfunc.DeCodeRsk.trialAction");
      }

    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "rskm1040_detl")) {
        wp.optionKey = wp.colStr("adj_limit_reason");
        dddwList("dddw_adj_limit_reason", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type ='ADJ_REASON_DOWN'");
      }

    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {

    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("建檔日期起迄: 輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 and A.stop_flag <>'Y' "
        + sqlCol(wp.itemStr("ex_batch_no"), "A.batch_no", "like%")
        + sqlCol(wp.itemStr("ex_date1"), "A.crt_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "A.crt_date", "<=");
    
    
    queryBefore(lsWhere);

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  void queryBefore(String lsWhere) {
	  sqlParm.setSqlParmNoClear(true);
    String sql1 = " select " + " A.apr_date " + " from rsk_trial_action A " + lsWhere
        + " order by apr_date Desc " + commSqlStr.rownum(1);
    sqlSelect(sql1);
    if (sqlRowNum > 0)
      wp.colSet("ex_apr_date", sqlStr("apr_date"));
    sqlParm.setSqlParmNoClear(true);
    String sql2 = " select " + " sum(decode(A.apr_flag,'Y',1,0)) as ex_apr_rows , "
        + " sum(decode(A.apr_flag,'N',1,0)) as ex_un_apr_rows " + " from rsk_trial_action A "
        + lsWhere;
    sqlSelect(sql2);
    wp.colSet("ex_apr_rows", "" + sqlNum("ex_apr_rows"));
    wp.colSet("ex_un_apr_rows", "" + sqlNum("ex_un_apr_rows"));

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = ""
        + " (select max(apr_date) from rsk_trial_action where batch_no =A.batch_no and apr_flag ='Y' and stop_flag<>'Y') ,  "
        + " batch_no ," + " batch_remark ," + " risk_group ," + " action_code ,"
        + " adj_limit_rate ," + " adj_limit_reason ," + " decode(msg_flag,'Y','Y','') as msg_flag ,"
        + " block_reason4 ," + " block_reason5 ," + " spec_status ," + " delay_action_day ,"
        // + " delay_msg_flag , "
        + " decode(delay_msg_flag,'Y','Y','') as delay_msg_flag ," + " loan_flag ,"
        + " decode(loan_flag,'0','正常','1','加強催理') as tt_loan_flag ," + " crt_date ," + " crt_user ,"
        + " apr_date ," + " apr_user ";
    wp.daoTable = "rsk_trial_action A ";
    wp.whereOrder = " order by 1 Desc , A.batch_no , A.risk_group Asc  ";
    pageQuery();


    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfter();
    wp.setListCount(1);
    wp.setPageValue();
  }

  void queryAfter() {
    String sql1 = " select " + " wf_desc " + " from ptr_sys_idtab "
        + " where wf_type ='RSK_ACTION_VERSION' " + " and wf_id = ? ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "batch_no")});
      if (sqlRowNum > 0)
        wp.colSet(ii, "action_desc", sqlStr("wf_desc"));
    }
  }

  @Override
  public void querySelect() throws Exception {
    batchNo = wp.itemStr("data_k1");
    riskGroup = wp.itemStr("data_k2");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    setDddwExActionCode();
    if (empty(batchNo)) {
      batchNo = itemkk("batch_no");
    }
    if (empty(riskGroup)) {
      riskGroup = itemkk("risk_group");
    }
    wp.selectSQL = "hex(rowid) as rowid , mod_seqno," + " batch_no," + " apr_flag,"
        + " action_code," + " risk_group," + " delay_action_day," + " delay_msg_flag,"
        + " msg_flag," + " adj_limit_rate," + " adj_limit_reason," + " block_reason4,"
        + " block_reason5," + " spec_status," + " loan_flag," + " crt_user," + " crt_date,"
        + " to_char(mod_time,'yyyymmdd') as mod_date," + " mod_user," + " apr_date," + " apr_user";
    wp.daoTable = "rsk_trial_action";
    wp.whereStr =
        " where 1=1 and apr_flag='N'" + sqlCol(batchNo, "batch_no") + sqlCol(riskGroup, "risk_group");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {

      wp.selectSQL = "hex(rowid) as rowid , mod_seqno," + " batch_no," + " apr_flag,"
          + " action_code," + " risk_group," + " delay_action_day," + " delay_msg_flag,"
          + " msg_flag," + " adj_limit_rate," + " adj_limit_reason," + " block_reason4,"
          + " block_reason5," + " spec_status," + " loan_flag," + " crt_user," + " crt_date,"
          + " to_char(mod_time,'yyyymmdd') as mod_date," + " mod_user," + " apr_date , "
          + " apr_user";
      wp.daoTable = "rsk_trial_action";
      wp.whereStr =
          " where 1=1 and apr_flag='Y'" + sqlCol(batchNo, "batch_no") + sqlCol(riskGroup, "risk_group");
      this.logSql();
      pageSelect();
      if (sqlNotFind()) {
        alertErr("查無資料, key=" + batchNo);
        return;
      }
    }

    selectActionDesc();

  }

  void selectActionDesc() {
    String sql1 = " select " + " wf_desc " + " from ptr_sys_idtab "
        + " where wf_type ='RSK_ACTION_VERSION' " + " and wf_id = ? ";

    sqlSelect(sql1, new Object[] {wp.colStr("batch_no")});
    if (sqlRowNum <= 0)
      return;
    wp.colSet("action_desc", sqlStr("wf_desc"));

  }

  @Override
  public void saveFunc() throws Exception {
    rskm02.Rskm1040Func func = new rskm02.Rskm1040Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    this.sqlCommit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
    } else
      saveAfter(false);


  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      btnModeAud();
    }

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  public void wfAjaxKey() throws Exception {
    // super.wp = wr; // 20200102 updated for archit. change

    // String ls_winid =
    selectData1(wp.itemStr("ax_key"));
    if (rc != 1) {
      wp.addJSON("action_desc", "");
      return;
    }
    wp.addJSON("action_desc", sqlStr("wf_desc"));
  }

  void selectData1(String wfID) {
    String sql1 = " select " + " wf_desc " + " from ptr_sys_idtab "
        + " where wf_type ='RSK_ACTION_VERSION' " + " and wf_id = ? ";

    sqlSelect(sql1, new Object[] {wfID});
    if (sqlRowNum <= 0)
      return;
  }

  void setDddwExActionCode() {
    StringBuffer sb = new StringBuffer("");
    sb.append("<option value='0'>01.</option>" + wp.newLine);
    sb.append("<option value='1'>02.</option>" + wp.newLine);
    sb.append("<option value='2'>03.</option>" + wp.newLine);
    sb.append("<option value='3'>04.</option>" + wp.newLine);
    sb.append("<option value='4'>05.</option>" + wp.newLine);
    sb.append("<option value='5'>06.</option>" + wp.newLine);
    sb.append("<option value='6'>07.</option>" + wp.newLine);
    sb.append("<option value='7'>08.</option>" + wp.newLine);
    sb.append("<option value='8'>09.</option>" + wp.newLine);
    wp.colSet("dddw_action_code", sb.toString());
  }

}
