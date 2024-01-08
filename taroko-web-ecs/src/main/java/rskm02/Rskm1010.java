/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
******************************************************************************/
package rskm02;
/**
 * 2019-1219:  Alex  ptr_branch -> gen_brn
 * 2019-1206:  Alex  add initButton
 * 2019-1105   JH    detl: AJAX-insert
 * 2019-0516:  JH    modify
 *
 */

import ofcapp.*;
import taroko.com.TarokoCommon;

public class Rskm1010 extends BaseEdit {
  rskm02.Rskm1010Func func;
  String batchNo = "", kk2 = "", kk3 = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      /*-資料讀取-*/
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "R2")) {
      /*-資料讀取-*/
      strAction = "R2";
      detl2Read();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      if (colIsEmpty("rowid")) {
        strAction = "A";
        insertFunc();
      } else
        updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "S2") || eqIgno(wp.buttonCode, "R2")) {
      // -讀取明細資料-
      strAction = "R2";
      detl2Read();
    } else if (eqIgno(wp.buttonCode, "U2")) {
      // -存檔-明細資料-
      strAction = "U2";
      detl2Insert();
    } else if (eqIgno(wp.buttonCode, "D2")) {
      // -存檔-明細資料-
      strAction = "U2";
      detl2Delete();
    } else if (eqIgno(wp.buttonCode, "U3")) {
      // -存檔-明細資料-
      strAction = "U3";
      // detl2_import();
    } else if (eqIgno(wp.buttonCode, "R1")) {
      // -reset 樣板資料-
      strAction = "R1";
      batchCopyProc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      procFunc();
      // } else if (eq_igno(wp.buttonCode,"C5")) {
      // procFuncDetail();
    } else if (eqIgno(wp.buttonCode, "C6")) {
      copyProc();
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      ajaxFunc();
      return;
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void dddwSelect() {
    try {
      // -_detl-
      if (posAny(wp.respHtml, "_detl") > 0) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr("db_copy_no");
        dddwList("dddw_copy_no", "rsk_trial_parm", "batch_no", "trial_reason",
            "where sample_flag='Y' and apr_flag='Y'");
        // -dddw_jcic_no-
        wp.initOption = "--";
        wp.optionKey = wp.colStr("jcic_no");
        dddwList("dddw_jcic_no", "col_jcic_query_mast", "jcic_no", "contract_desc", "where 1=1");
        // -dddw_score_type-
        wp.initOption = "--";
        wp.optionKey = wp.colStr("score_type");
        dddwList("dddw_score_type",
            "select DISTINCT score_type db_code, score_type||'_'||score_type_desc as db_desc"
                + " from rsk_score_type  where 1=1 order by 1");
        // -dddw_trial_action_no-
        wp.initOption = "--";
        wp.optionKey = wp.colStr("action_batch_no");
        dddwList("dddw_trial_action_no", "rsk_trial_action", "batch_no",
            "batch_no||'_'||(select wf_desc from ptr_sys_idtab where wf_type ='RSK_ACTION_VERSION' and wf_id =batch_no)||'_'||max(apr_date)",
            "where apr_date is not null and stop_flag <>'Y' group by batch_no");
      }
    } catch (Exception ex) {
      wp.expHandle(ex);
    }
    try {
      if (wp.respHtml.indexOf("_01_accttype") > 0) {
        wp.optionKey = wp.colStr(0, "ex_data_code");
        dddwList("dddw_accttype", "ptr_acct_type", "acct_type", "chin_name", "where 1=1");
      } else if (wp.respHtml.indexOf("_02_groupcode") > 0) {
        wp.optionKey = wp.colStr(0, "ex_data_code");
        dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
            "where group_code<>'0000'");
      } else if (wp.respHtml.indexOf("_05_compbank") > 0) {
        wp.optionKey = wp.colStr(0, "ex_data_code");
        dddwList("dddw_bank_no", "gen_brn", "branch", "full_chi_name", "where 1=1 ");
      } else if (wp.respHtml.indexOf("_10_exgroup") > 0) {
        wp.optionKey = wp.colStr(0, "ex_data_code");
        dddwList("dddw_group_code", "ptr_group_code", "group_code", "group_name",
            "where group_code<>'0000'");
      }
    } catch (Exception ex) {
    }
  }

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "rskm1010")) {
      btnModeAud("xx");
    }

    if (wp.respHtml.indexOf("_detl") > 0) {
      btnModeAud();
      if (wp.colEq("apr_flag", "Y")) {
        // this.btnOn_delete(false);
        this.btnUpdateOn(false);
        buttonOff("btnC7_disable");
      } else {
        buttonOff("btnCopy_disable");
      }
    }
    if (eqAny("U2", strAction) || eqAny("R2", strAction)) {
      if (itemEq("apr_flag", "Y")) {
        btnUpdateOn(false);
      }
    }
  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_query_date1"), wp.itemStr("ex_query_date2")) == false) {
      alertErr2("查詢日期: " + appMsg.errStrend);
      return;
    }
    wp.whereStr = "WHERE regist_type ='2' and nvl(sample_flag,'n')<>'Y'"
        + sqlCol(wp.itemStr("ex_batch_no"), "batch_no", "like%")
        + sqlCol(wp.itemStr("ex_copy_no"), "copy_batch_no", "like%")
        + sqlStrend(wp.itemStr("ex_query_date1"), wp.itemStr("ex_query_date2"), "query_date");

    if (!wp.itemEq("ex_apr_flag", "0")) {
      wp.whereStr += sqlCol(wp.itemStr("ex_apr_flag"), "apr_flag");
    }

    wp.whereOrder = " order by batch_no";

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " decode(apr_flag,'Y',apr_date,crt_date) as order_date ," + " batch_no ,"
        + " query_date ," + " trial_reason ," + " card_since_s_date ," + " card_since_e_date ,"
        + " crt_user ," + " crt_date ," + " apr_date ," + " copy_batch_no," + " apr_flag , "
        + " imp_jcic_date ";
    wp.daoTable = "rsk_trial_parm";
    wp.whereOrder = " order by apr_flag Asc , 1 Desc , batch_no";
    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2(appMsg.errCondNodata);
      return;
    }
    wp.setListCount(1);
    wp.totalRows = wp.dataCnt;
    wp.setPageValue();
  }


  @Override
  public void querySelect() throws Exception {
    batchNo = wp.itemStr("data_k1");
    // wp.col_set("kk_batch_no", kk1);
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(batchNo)) {
      batchNo = itemKk("batch_no");
    }

    if (empty(batchNo)) {
      alertErr2("[查詢批號] 不可空白");
      return;
    }

    wp.selectSQL = "A.*, hex(rowid) as rowid, " + "substrb(payment_rate1,1,1) as db_payrate01, "
        + "substrb(payment_rate1,7,1) as db_payrate07,  "
        + "substrb(payment_rate1,6,1) as db_payrate06,  "
        + "substrb(payment_rate1,5,1) as db_payrate05,  "
        + "substrb(payment_rate1,4,1) as db_payrate04,  "
        + "substrb(payment_rate1,2,1) as db_payrate02,  "
        + "substrb(payment_rate1,3,1) as db_payrate03,  " + "copy_batch_no as db_copy_no, "
        + "uf_2ymd(mod_time) as mod_date "
        + ", decode(A.regist_type,'2','批次參數','人工滙入') as tt_regist_type";
    wp.daoTable = "rsk_trial_parm A";
    wp.whereStr = "where nvl(sample_flag,'N')<>'Y' and regist_type='2'" + sqlCol(batchNo, "batch_no");
    // -order:未覆核,己覆核:取一筆---
    wp.whereOrder = " order by apr_flag " + commSqlStr.rownum(1);
    pageSelect();
    if (sqlRowNum <= 0) {
      alertErr2(appMsg.errDataNodata + ", key=" + batchNo);
      return;
    }

    procFuncDetail();
    // if (rc==1 && pos("|A|U",is_action)>0) {
    // wp.col_set("to_page_3"," || 1==1 ");
    // }

  }


  @Override
  public void saveFunc() throws Exception {

    if (wp.itemEmpty("imp_jcic_date") == false && (isUpdate() || isDelete())) {
      alertErr2("已送查JCIC不可異動或刪除");
      return;
    }

    if (isDelete() && wp.itemEq("apr_flag", "Y")) {
      if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
        return;
      }
    }
    func = new rskm02.Rskm1010Func();
    func.setConn(wp);

    rc = func.dbSave(strAction);

    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);

    // -call-page-3-
    if (rc == 1 && this.pos("|A|U", strAction) > 0) {
      userAction = true;
      dataRead();
    }
  }

  void detl2Read() throws Exception {
    wp.pageRows = 999;
    batchNo = wp.itemStr("data_k1");
    kk2 = wp.itemStr("data_k2");
    kk3 = wp.itemStr2("apr_flag");
    if (empty(batchNo)) {
      batchNo = wp.itemStr("batch_no");
    }
    if (empty(kk2)) {
      kk2 = wp.itemStr("data_type");
    }

    if (empty(batchNo) || empty(kk2)) {
      alertErr2("[查詢批號, 明細類別] 不可空白");
      return;
    }
    if (eqIgno(kk2, "01")) {
      // -acct_type-
      wp.sqlCmd = "select data_code, data_code2"
          + ",data_code||'_'||uf_tt_acct_type(data_code) as tt_data_code"
          + " from rsk_trial_parmdtl" + " where batch_no =? and data_type='01' and apr_flag=?";
    } else if (eqAny(kk2, "02")) {
      // -團代-
      wp.sqlCmd = "select data_code, data_code2"
          + ",data_code||'_'||uf_tt_group_code(data_code) as tt_data_code"
          + " from rsk_trial_parmdtl" + " where batch_no =? and data_type='02' and apr_flag=?";
    } else if (eqAny(kk2, "03")) {
      // -class_code-
      wp.sqlCmd = "select data_code" + " from rsk_trial_parmdtl"
          + " where batch_no =? and data_type='03' and apr_flag=?";
    } else if (eqAny(kk2, "04")) {
      // -PD rating-
      wp.sqlCmd = "select data_code" + " from rsk_trial_parmdtl"
          + " where batch_no =? and data_type='04' and apr_flag=?";
    } else if (eqAny(kk2, "05")) {
      // -商務卡受理行-
      wp.sqlCmd = "select A.data_code" + ",A.data_code||'_'||B.full_chi_name as tt_data_code"
          + " from rsk_trial_parmdtl A left join gen_brn B on B.branch=A.data_code"
          + " where A.batch_no =? and A.data_type='05' and A.apr_flag=?";
    } else if (eqAny(kk2, "06")) {
      // -調額代碼-
      wp.sqlCmd = "select data_code" + " from rsk_trial_parmdtl"
          + " where batch_no =? and data_type='06' and apr_flag=?";
    } else if (eqAny(kk2, "07")) {
      // -風險族群-
      wp.sqlCmd = "select data_code" + " from rsk_trial_parmdtl"
          + " where batch_no =? and data_type='07' and apr_flag=?";
    } else if (eqAny(kk2, "08")) {
      // -排除條件--凍結碼-
      wp.sqlCmd = "select data_code" + " from rsk_trial_parmdtl"
          + " where batch_no =? and data_type='08' and apr_flag=?";
    } else if (eqAny(kk2, "09")) {
      // -指定凍結碼-
      wp.sqlCmd = "select data_code" + " from rsk_trial_parmdtl"
          + " where batch_no =? and data_type='09' and apr_flag=?";
    } else if (eqAny(kk2, "10")) {
      // -排除團體代號-
      wp.sqlCmd = "select data_code" + " from rsk_trial_parmdtl"
          + " where batch_no =? and data_type='10' and apr_flag=?";
    }
    setString2(1, batchNo);
    setString(kk3);

    pageQuery();
    if (sqlNotFind()) {
      wp.notFound = "N";
    }
    wp.setListCount(1);
    wp.colSet("IND_NUM", "" + wp.selectCnt);
  }



  void detlReadY() throws Exception {
    batchNo = wp.itemStr("data_k1");
    kk2 = wp.itemStr("data_k2");
    if (empty(batchNo)) {
      batchNo = wp.itemStr("batch_no");
    }
    if (empty(kk2)) {
      kk2 = wp.itemStr("data_type");
    }

    if (empty(batchNo) || empty(kk2)) {
      alertErr2("[查詢樣板批號, 明細類別] 不可空白");
      return;
    }
    if (eqIgno(kk2, "01")) {
      // -acct_type-
      wp.selectSQL = "data_code, " + "data_code2  " + ",data_code||'_'||" + wp.sqlID
          + "uf_tt_acct_type(data_code) as tt_data_code";
    } else if (eqAny(kk2, "02")) {
      // -團代-
      wp.selectSQL = "data_code, " + "data_code2  " + ",data_code||'_'||" + wp.sqlID
          + "uf_tt_group_code(data_code) as tt_data_code";
    } else if (eqAny(kk2, "03")) {
      // -class_code-
      wp.selectSQL = "data_code " + "";
    } else if (eqAny(kk2, "04")) {
      // -PD rating-
      wp.selectSQL = "data_code " + "";
    } else if (eqAny(kk2, "05")) {
      // -商務卡受理行-
      wp.selectSQL = "A.data_code, " + " A.data_code||'_'||" + " B.full_chi_name as tt_data_code";
      wp.daoTable = "rsk_trial_parmdtl A left join gen_brn B " + " on A.data_code = B.branch";
      wp.whereStr = " where A.batch_no='" + batchNo + "'" + " and A.data_type ='" + kk2 + "'";
    } else if (eqAny(kk2, "06")) {
      // -調額代碼-
      wp.selectSQL = "data_code " + "";
    } else if (eqAny(kk2, "07")) {
      // -風險族群-
      wp.selectSQL = "data_code " + "";
    } else if (eqAny(kk2, "08")) {
      // -排除條件--凍結碼-
      wp.selectSQL = "data_code " + "";
    } else if (eqAny(kk2, "09")) {
      // -指定凍結碼-
      wp.selectSQL = "data_code " + "";
    } else if (eqAny(kk2, "10")) {
      // -排除團體代號-
      wp.selectSQL = "data_code " + "";
    }

    if (empty(wp.daoTable)) {
      wp.daoTable = "rsk_trial_parmdtl";
    }
    if (empty(wp.whereStr)) {
      wp.whereStr = " where batch_no='" + batchNo + "'" + " and data_type ='" + kk2 + "'";
    }
    wp.whereStr += " and apr_flag ='Y'";

    pageQuery();
    if (sqlRowNum <= 0) {
      wp.notFound = "N";
    }
    // wp.setListCount(1);
    // wp.col_set("IND_NUM",""+wp.selectCnt);
  }

  void ajaxFunc() throws Exception {
    if (wp.itemEq("apr_flag", "Y")) {
      alertErr("資料己覆核, 不可新增");
      return;
    }
    // -insert detl-
    rskm02.Rskm1010Func func = new rskm02.Rskm1010Func();
    func.setConn(wp);

    rc = func.dbInsertDetl();
    if (rc == 1) {
      func.mastDetlSynch(wp.colStr("batch_no"));
    }
    sqlCommit(rc);

    if (rc != 1) {
      wp.addJSON("ex_errmsg", func.getMsg());
      // err_alert(func.getMsg());
    } else {
      wp.addJSON("ax_errmsg", "");
    }
  }

  void detl2Insert() throws Exception {
    rskm02.Rskm1010Func func = new rskm02.Rskm1010Func();
    func.setConn(wp);
    wp.listCount[0] = wp.itemRows("data_code");

    // if(wp.item_eq("data_type", "08") && wp.item_rows("data_code")>=20){
    // err_alert("排除條件-凍結碼：不可超過20個!");
    // return ;
    // }

    rc = func.dbInsertDetl();
    if (rc == 1) {
      func.mastDetlSynch(wp.colStr("batch_no"));
    }
    sqlCommit(rc);

    if (rc != 1) {
      alertErr2(func.getMsg());
    } else {
      wp.colSet("ex_data_code", "");
      alertMsg("明細新增完成");
    }
  }

  void batchCopyProc() {
    if (wp.itemEmpty("kk_batch_no") || wp.itemEmpty("db_copy_no")) {
      alertErr2("查詢批號, 指定樣板批號: 不可空白");
      return;
    }

    // busi.
    // busi.rskm02.Rskm1010Func func = new busi.rskm02.Rskm1010Func(wp);
    rskm02.Rskm1010Func func = new rskm02.Rskm1010Func();
    func.setConn(wp);
    rc = func.batchCopyProc();
    sqlCommit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
      return;
    }
    alertMsg("批號參數: 複製成功");

    try {
      dataRead();
    } catch (Exception ex) {
    }

  }

  void selectRskTrialParm() {
    String lsBatchNo = wp.itemStr("db_copy_no");
    if (empty(lsBatchNo)) {
      alertErr2("指定樣板批號: 不可空白");
      return;
    }
    wp.selectSQL = "batch_no as db_copy_no, " + "trial_reason, " + "jcic_no, "
        + "card_since_s_date, " + "card_since_e_date, " + "payment_rate1_cond, " + "payment_rate1, "
        + "all_credit_limit_cond, " + "all_credit_limit_s, " + "all_credit_limit_e, "
        + "credit_limit_cond, " + "credit_limit_s, " + "credit_limit_e, " + "group_code_cond, "
        + "class_code_cond, " + "pd_rating_cond, " + "reg_bank_no_cond, " + "adj_limit_cond, "
        + "adj_limit_s_date, " + "adj_limit_e_date, " + "rc_avg_rate_cond, " + "rc_avg_rate_mm, "
        + "rc_avg_rate1, " + "rc_avg_rate2, " + "prepay_cash_cond, " + "prepay_cash_mm, "
        + "prepay_cash_num, " + "limit_avguse_rate_cond, " + "limit_avguse_rate_mm, "
        + "limit_avguse_rate, " + "payment_rate_cond, " + "payment_rate_mm, " + "payment_rate_num, "
        + "no_debt_cond, " + "no_debt_mm, " + "payment_integ_cond, " + "acct_jrnl_bal_cond, "
        + "acct_jrnl_bal_s, " + "acct_jrnl_bal_e, " + "excl_retrial_cond, " + "excl_retrial_mm, "
        + "excl_block_cond, " + "regist_type, " + " substrb(payment_rate1,1,1) as db_payrate01, "
        + "substrb(payment_rate1,7,1) as db_payrate07,  "
        + "substrb(payment_rate1,6,1) as db_payrate06,  "
        + "substrb(payment_rate1,5,1) as db_payrate05,  "
        + "substrb(payment_rate1,4,1) as db_payrate04,  "
        + "substrb(payment_rate1,2,1) as db_payrate02,  "
        + "substrb(payment_rate1,3,1) as db_payrate03,  " + "risk_group_cond,  " + "batch_seqno, "
        + "score_type, " + "excl_asset_cond, " + "block_reason_cond, " + "excl_m1_cond, "
        + "excl_m1_mm, " + "excl_m1_cnt, " + "excl_0d_cond, " + "excl_0d_mm, " + "excl_0d_cnt, "
        + "excl_m2_cond, " + "excl_m2_mm, " + "excl_m2_cnt, " + "excl_group_code_cond, "
        + "has_card_cond, " + "has_card_value, " + "assign_list_cond, " + "assign_list_mm, "
        + "asset_ch_flag, " + "asset_value_flag, " + "copy_batch_no, " + "asset_cond, "
        + "parm3_proc_date, " +
        // "copy_batch_no as db_copy_no, " +
        "action_batch_no,  " + "crt_user, " + "crt_date, " + "apr_date, " + "apr_user, "
        + "imp_file_name, " + "list_crt_date, " + "list_crt_rows, " + "imp_jcic_date, "
        + "imp_jcic_user, " + "imp_jcic_rows, " + "mod_user, uf_2ymd(mod_time) as mod_date";

    wp.daoTable = "rsk_trial_parm";
    wp.whereStr = " where batch_no ='" + lsBatchNo + "'" + " and nvl(sample_flag,'n')='Y'";
    pageSelect();
    if (sqlRowNum <= 0) {
      alertErr2("指定樣板批號: 不存在");
    }
    // wp.item_set("db_corp_no", daoTid);

  }

  void procFunc() throws Exception {

    String[] aaBatch = wp.itemBuff("batch_no");
    String[] aaApr = wp.itemBuff("apr_flag");
    wp.listCount[0] = wp.itemRows("batch_no");
    String[] aaOpt = wp.itemBuff("opt");
    if (wp.itemRows("opt") <= 0) {
      alertErr2("請點選覆核資料");
      return;
    }
    this.optNumKeep(wp.listCount[0], aaOpt);
    if (checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")) == false) {
      return;
    }

    func = new rskm02.Rskm1010Func();
    func.setConn(wp);
    int llOk = 0, llCnt = 0;
    func.varsSet("apr_user", wp.itemStr("approval_user"));
    int rr = -1;
    for (int ii = 0; ii < aaOpt.length; ii++) {
      rr = (int) (toNum(aaOpt[ii]) - 1);
      if (rr < 0)
        continue;
      llCnt++;

      wp.colSet(rr, "ok_flag", "");
      if (eqIgno(aaApr[rr], "Y")) {
        // err_alert("資料已覆核");
        wp.colSet(rr, "ok_flag", "-");
        continue;
      }

      func.varsSet("batch_no", aaBatch[rr]);
      if (func.dataApprove() == 1) {
        this.dbCommit();
        wp.colSet(rr, "ok_flag", "V");
        llOk++;
      } else {
        this.dbRollback();
        wp.colSet(rr, "ok_flag", "X");
      }
    }
    alertMsg("資料處理筆數=" + llCnt + ", 成功筆數=" + llOk);
  }

  void procFuncDetail() {
    String ttDetl = "";
    if (wp.colEq("group_code_cond", "Y")) {
      ttDetl = selectDetlData("02", 10);
    } else {
      ttDetl = selectDetlData("01", 20);
    }
    wp.colSet("wk_01_detl", ttDetl);
    // -03.卡人等級-
    ttDetl = selectDetlData("03", 20);
    wp.colSet("wk_03_detl", ttDetl);
    // -04.PD Rating違約預測評等-
    ttDetl = selectDetlData("04", 20);
    wp.colSet("wk_04_detl", ttDetl);
    // -05.商務卡受理行(20)-
    ttDetl = selectDetlData("05", 20);
    wp.colSet("wk_05_detl", ttDetl);
    // -06.調額代碼-
    ttDetl = selectDetlData("06", 20);
    wp.colSet("wk_06_detl", ttDetl);
    // -07.風險族群-
    ttDetl = selectDetlData("07", 20);
    wp.colSet("wk_07_detl", ttDetl);
    // -08.凍結碼-
    ttDetl = selectDetlData("08", 20);
    wp.colSet("wk_08_detl", ttDetl);
    // -09.指定凍結碼-
    ttDetl = selectDetlData("09", 20);
    wp.colSet("wk_09_detl", ttDetl);
    // -10.排除 團體代號-
    ttDetl = selectDetlData("10", 10);
    wp.colSet("wk_10_detl", ttDetl);
  }

  String selectDetlData(String aType, int aiCnt) {
    String ttData = "";
    // if(empty(kk1)) kk1 = wp.item_ss("batch_no");
    // if(empty(kk2)) kk2 = wp.item_ss("apr_flag");
    String sql1 = " select " + " data_code " + " from rsk_trial_parmdtl " + " where 1=1 "
        + " and batch_no  =? " + " and data_type =? " + " and decode(apr_flag,'Y','Y','N') =? ";
    setString2(1, wp.colStr("batch_no"));
    setString(aType);
    setString(wp.colStr("apr_flag"));
    sqlSelect(sql1);
    if (sqlRowNum <= 0)
      return "";

    int llNrow = sqlRowNum;
    int liCnt = aiCnt;
    for (int ll = 0; ll < llNrow; ll++) {
      ttData += sqlStr(ll, "data_code") + ",";
      liCnt--;
      if (liCnt <= 0)
        break;
    }
    if (llNrow > aiCnt) {
      ttData += " ... 共計 " + sqlRowNum + " 個";
    }
    return ttData;

  }

  @Override
  public void initPage() {
    if (eqIgno(wp.respHtml, "rskm1010_detl") && eqIgno(strAction, "new")) {
      wp.colSet("jcic_no", "814");
      wp.colSet("score_type", "001");
      wp.colSet("regist_type", "2");
      wp.colSet("tt_regist_type", "2.批次參數");
    }
  }

  void detl2Delete() throws Exception {
    rskm02.Rskm1010Func func = new rskm02.Rskm1010Func();
    func.setConn(wp);
    int ilCnt = 0, ilOk = 0, ilErr = 0;

    String[] aaOpt = wp.itemBuff("opt");
    String[] lsDataCode = wp.itemBuff("data_code");
    wp.listCount[0] = wp.itemRows("data_code");
    func.varsSet("batch_no", wp.itemStr2("batch_no"));
    func.varsSet("data_type", wp.itemStr2("data_type"));
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
    if (ilOk > 0) {
      func.mastDetlSynch(wp.colStr("batch_no"));
    }

    if (ilCnt == 0) {
      alertErr2("請選擇要刪除的資料");
      return;
    }

    alertMsg("刪除明細完成,成功:" + ilOk + " 失敗:" + ilErr);

  }

  void copyProc() throws Exception {
    rskm02.Rskm1010Func func = new rskm02.Rskm1010Func();
    func.setConn(wp);

    if (wp.itemEmpty("imp_jcic_date") == false) {
      alertErr2("已送查JCIC不可異動或刪除");
      return;
    }

    rc = func.copyProc();
    sqlCommit(rc);

    if (rc != 1) {
      errmsg(func.getMsg());
    } else {
      batchNo = wp.itemStr2("batch_no");
      dataRead();
      wp.respMesg = "異動處理完成";
    }

  }


}
