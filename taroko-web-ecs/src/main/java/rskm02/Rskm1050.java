/**
 * 2019-1206:  Alex  add initButton
 * 2019-1118:  Alex  query add id_code
 * 2019-0619:  JH    p_xxx >>acno_pxxx
 * 2018-0823   Alex    modify
 * 109-04-27  V1.00.05  Tanwei       updated for project coding standard
 * 109-12-31  V1.00.06   shiyuqi       修改无意义命名
* 110-01-05  V1.00.07  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *  *   
 */
package rskm02;

import ofcapp.BaseAction;
import ofcapp.InfacePdf;
import taroko.com.TarokoPDF;

public class Rskm1050 extends BaseAction implements InfacePdf {
  taroko.base.CommDate commDate = new taroko.base.CommDate();
  String batchNo = "", idPSeqno = "";
  String isBatchNo = "", isIdPSeqno = "", isErrorDesc = "";

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
      updateProc();
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
      dataRead2();
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "C1")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
      strAction = "PDF";
      printData();
    } else if (eqIgno(wp.buttonCode, "PDF1")) { // -PDF-
      strAction = "PDF";
      pdfPrint1();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "rskm1050")) {
        wp.optionKey = wp.colStr("ex_risk_group");
        dddwList("dddw_risk_group", "rsk_trial_parm2", "risk_group", "risk_group",
            "where 1=1 and apr_flag = 'Y' order by risk_group");
      }

      if (eqIgno(wp.respHtml, "rskm1050")) {
        wp.optionKey = wp.colStr("ex_trial_user");
        dddwList("dddw_trial_user", "sec_user", "usr_id", "usr_cname", commSqlStr.dddwUserType);
      }

      if (eqIgno(wp.respHtml, "rskm1050")) {
        wp.optionKey = wp.colStr("ex_action_code");
        ddlbList("dddw_trial_action", wp.colStr("ex_action_code"),
            "ecsfunc.DeCodeRsk.trial_action");
      }

      if (eqIgno(wp.respHtml, "rskm1050_detl")) {
        wp.optionKey = wp.colStr("A1_action_code");
        ddlbList("dddw_trial_action", wp.colStr("A1_action_code"),
            "ecsfunc.DeCodeRsk.trial_action");
      }

      if (eqIgno(wp.respHtml, "rskm1050_detl")) {
        wp.optionKey = wp.colStr("A1_adj_credit_limit_reason");
        dddwList("d_rsk_iddesc_dw00",
            "select wf_id as db_code , wf_id||'.'||wf_desc as db_desc from ptr_sys_idtab where wf_type = 'ADJ_REASON_DOWN' ");
      }

    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (chkStrend(wp.itemStr("ex_query_date1"), wp.itemStr("ex_query_date2")) == false) {
      alertErr2("查詢日期 : 起迄錯誤");
      return;
    }

    if (chkStrend(wp.itemStr("ex_trial_date1"), wp.itemStr("ex_trial_date2")) == false) {
      alertErr2("覆審日期 : 起迄錯誤");
      return;
    }

    if (empty(wp.itemStr("ex_batch_no")) && empty(wp.itemStr("ex_idno"))
        && empty(wp.itemStr("ex_query_date1")) && empty(wp.itemStr("ex_query_date2"))
        && empty(wp.itemStr("ex_trial_date1")) && empty(wp.itemStr("ex_trial_date2"))) {
      alertErr2("請輸入查詢條件");
      return;
    }

    String lsWhere = " where 1=1 " + sqlCol(wp.itemStr("ex_batch_no"), "A.batch_no")
        + sqlCol(wp.itemStr("ex_query_date1"), "A.query_date", ">=")
        + sqlCol(wp.itemStr("ex_query_date2"), "A.query_date", "<=")
        + sqlCol(wp.itemStr("ex_trial_date1"), "A.trial_date", ">=")
        + sqlCol(wp.itemStr("ex_trial_date2"), "A.trial_date", "<=")
        + sqlCol(wp.itemStr("ex_risk_group"), "A.risk_group")
        + sqlCol(wp.itemStr("ex_action_code"), "A.action_code")
        + sqlCol(wp.itemStr("ex_close_flag"), "A.close_flag")
        + sqlCol(wp.itemStr("ex_trial_user"), "A.trial_user")
        + sqlCol(wp.itemStr("ex_trial_user"), "A.trial_user")
        + sqlCol(wp.itemStr("ex_rc_loan"), "uf_nvl(B.ecs044,'N')")
        + sqlCol(wp.itemStr("ex_comp_card"), "uf_nvl(B.ecs045,'N')")
        + sqlCol(wp.itemStr("ex_spec_list"), "uf_nvl(B.ecs047,'N')")
        + sqlCol(wp.itemStr("ex_jcjic002_04"), "C.jcic002_04", ">=");

    if (!wp.itemEmpty("ex_idno")) {
      lsWhere += " and A.id_p_seqno in (select id_p_seqno from crd_idno where id_no ='"
          + wp.itemStr("ex_idno") + "')";
    }

    if (eqIgno(wp.itemStr("ex_parm3_type"), "Y")) {
      lsWhere += " and A.parm3_trial_type ='1' ";
    }
    // --擔保
    if (!empty(wp.itemStr("ex_ecs046"))) {
      lsWhere += " and decode(B.ecs046,'Y','1','N','0','','0',B.ecs046) ='"
          + wp.itemStr("ex_ecs046") + "' ";
    }
    // --指定覆審
    if (!empty(wp.itemStr("ex_assign_flag"))) {
      lsWhere += sqlCol(wp.itemStr("ex_assign_flag"), "uf_nvl(A.assign_list_flag,'N')");
    }
    // --信用卡近1M遲繳
    if (!empty(wp.itemStr("ex_jcic012_1"))) {
      lsWhere += sqlCol(wp.itemStr("ex_jcic012_1"), "uf_nvl(C.jcic012_01,'N')");
    }
    // --近6M遲繳次
    if (wp.itemNum("ex_jcic012_3b") > 0) {
      lsWhere += sqlCol(wp.itemStr("ex_jcic012_3b"), "nvl(C.jcic012_03,0)", ">=");
    }
    // --近6M遲繳次
    if (wp.itemNum("ex_jcic012_3e") > 0) {
      lsWhere += sqlCol(wp.itemStr("ex_jcic012_3e"), "nvl(C.jcic012_03,0)", "<=");
    }
    // --近12月有M2
    if (wp.itemEq("ex_jcic012_5", "Y")) {
      lsWhere += " and C.jcic012_5 >0";
    } else if (wp.itemEq("ex_jcic012_5", "N")) {
      lsWhere += " and C.jcic012_5 =0";
    }
    // --循環額度使用率[%]
    if (wp.itemNum("ex_jcic008_2") != 0) {
      lsWhere += sqlCol(wp.itemStr("ex_jcic008_2"), "C.jcic008_2", ">=");
    }
    // --授信逾期
    if (!empty(wp.itemStr("ex_jcic019"))) {
      lsWhere += sqlCol(wp.itemStr("ex_jcic019"), "uf_nvl(C.jcic019,'N')");
    }
    // --授信逾期 學貸
    if (!empty(wp.itemStr("ex_jcic019_01"))) {
      lsWhere += sqlCol(wp.itemStr("ex_jcic019_01"), "uf_nvl(C.jcic019_01,'N')");
    }
    // --6M 無擔遲繳次
    if (wp.itemNum("ex_jcic023_1b") > 0) {
      lsWhere += sqlCol(wp.itemStr("ex_jcic023_1b"), "C.jcic023_01", ">=");
    }
    // --6M 無擔遲繳次
    if (wp.itemNum("ex_jcic023_1e") > 0) {
      lsWhere += sqlCol(wp.itemStr("ex_jcic023_1e"), "C.jcic023_01", "<=");
    }
    // --6M 有擔遲繳次
    if (wp.itemNum("ex_jcic023_2b") > 0) {
      lsWhere += sqlCol(wp.itemStr("ex_jcic023_2b"), "C.jcic023_02", ">=");
    }
    // --6M 有擔遲繳次
    if (wp.itemNum("ex_jcic023_2e") > 0) {
      lsWhere += sqlCol(wp.itemStr("ex_jcic023_2e"), "C.jcic023_02", "<=");
    }
    // --無擔負債
    if (wp.itemNum("ex_jcic039b") > 0) {
      lsWhere += sqlCol(wp.itemStr("ex_jcic039b"), "C.jcic039", ">=");
    }
    // --無擔負債
    if (wp.itemNum("ex_jcic039e") > 0) {
      lsWhere += sqlCol(wp.itemStr("ex_jcic039e"), "C.jcic039", "<=");
    }
    // --本行 RC 戶且 DBR>22
    if (eqIgno(wp.itemStr("ex_ecs034_jcic040"), "Y")) {
      lsWhere += " and (B.ecs034>0 and C.jcic040>22) ";
    } else if (eqIgno(wp.itemStr("ex_ecs034_jcic040"), "N")) {
      lsWhere += " and (not (B.ecs034>0 and C.jcic040>22)) ";
    }
    // --頂級卡戶
    if (eqIgno(wp.itemStr("ex_card_note"), "Y")) {
      lsWhere += " and A.card_note_i='Y' ";
    } else if (eqIgno(wp.itemStr("ex_card_note"), "N")) {
      lsWhere += " and A.card_note_i in ('','N') ";
    }
    // --特殊名單
    // ls_where += sql_col(wp.item_ss("ex_spec_list"),"B.ecs047");
    if (wp.itemEq("ex_data_flag1", "Y") || wp.itemEq("ex_data_flag2", "Y")
        || wp.itemEq("ex_data_flag3", "Y") || wp.itemEq("ex_data_flag4", "Y")
        || wp.itemEq("ex_data_flag5", "Y") || wp.itemEq("ex_data_flag6", "Y")) {

      String lsDataFlag = "";
      if (wp.itemEq("ex_data_flag1", "Y")) {
        if (lsDataFlag.length() == 0) {
          lsDataFlag += " and A.id_no in (select id_no from rsk_gage_list where ";
          lsDataFlag += " substr(data_flag,1,1) ='A' ";
        } else {
          lsDataFlag += " or substr(data_flag,1,1) ='A' ";
        }
      }

      if (wp.itemEq("ex_data_flag2", "Y")) {
        if (lsDataFlag.length() == 0) {
          lsDataFlag += " and A.id_no in (select id_no from rsk_gage_list where ";
          lsDataFlag += " substr(data_flag,2,1) ='B' ";
        } else {
          lsDataFlag += " or substr(data_flag,2,1) ='B' ";
        }
      }

      if (wp.itemEq("ex_data_flag3", "Y")) {
        if (lsDataFlag.length() == 0) {
          lsDataFlag += " and A.id_no in (select id_no from rsk_gage_list where ";
          lsDataFlag += " substr(data_flag,3,1) ='C' ";
        } else {
          lsDataFlag += " or substr(data_flag,3,1) ='C' ";
        }
      }

      if (wp.itemEq("ex_data_flag4", "Y")) {
        if (lsDataFlag.length() == 0) {
          lsDataFlag += " and A.id_no in (select id_no from rsk_gage_list where ";
          lsDataFlag += " substr(data_flag,4,1) ='D' ";
        } else {
          lsDataFlag += " or substr(data_flag,4,1) ='D' ";
        }
      }

      if (wp.itemEq("ex_data_flag5", "Y")) {
        if (lsDataFlag.length() == 0) {
          lsDataFlag += " and A.id_p_seqno in (select id_p_seqno from rsk_gage_list where ";
          lsDataFlag += " substr(data_flag,5,1) ='E' ";
        } else {
          lsDataFlag += " or substr(data_flag,5,1) ='E' ";
        }
      }

      if (wp.itemEq("ex_data_flag6", "Y")) {
        if (lsDataFlag.length() == 0) {
          lsDataFlag += " and A.id_no in (select id_no from rsk_gage_list where ";
          lsDataFlag += " substr(data_flag,6,1) ='F' ";
        } else {
          lsDataFlag += " or substr(data_flag,6,1) ='F' ";
        }
      }

      lsDataFlag += " ) ";
      lsWhere += lsDataFlag;
    }



    // --近2次覆審總無擔餘額增加
    lsWhere += sqlCol(wp.itemStr("ex_jcic038"), "C.jcic038", ">=");
    // --循環信用餘額
    lsWhere += sqlCol(wp.itemStr("ex_jcic002_04"), "C.jcic002_04", ">=");
    // --評等
    lsWhere += sqlCol(wp.itemStr("ecs_jcic_level1"), "A.ecs_jcic_level", ">=")
        + sqlCol(wp.itemStr("ecs_jcic_level2"), "A.ecs_jcic_level", "<=");
    // --授信逾期
    lsWhere += sqlCol(wp.itemStr("ex_jcic019"), "C.jcic019");


    lsWhere += " and ( uf_nvl(A.trial_type,'1')='1'"
        + " or (A.trial_type='2' and A.action_date='' and A.close_date='')) ";

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " A.risk_group ," + " A.id_no ," + " A.id_code ," + " A.batch_no ,"
        + " A.id_p_seqno ," + " uf_idno_name(A.id_p_seqno) as chi_name ," + " A.query_date ,"
        + " A.action_code ," + " A.trial_remark||A.trial_remark2||A.trial_remark3 as trial_remark ,"
        + " substr(A.trial_remark||A.trial_remark2||A.trial_remark3,1,30) as trial_remark_30 ,"
        + " A.block_reason ," + " A.trial_type ," + " A.trial_date ," + " A.trial_user ,"
        + " A.close_flag ," + " A.close_user ," + " A.close_date ," + " A.loan_flag ,"
        + " A.assign_list_flag ," + " A.card_note_i ," + " A.block_reason5 ,"
        + " A.adj_credit_limit_reason ," + " A.parm3_trial_type ," + " A.spec_status ,"
        + " B.ecs044 ," + " B.ecs045 ," + " B.ecs046 ," + " B.ecs047 ," + " B.ecs034 ,"
        + " C.jcic012_01 ," + " C.jcic012_03 ," + " C.jcic019 ," + " C.jcic023_01 ,"
        + " C.jcic023_02 ," + " C.jcic039 ," + " C.jcic040 ," + " '' as xxx ";

    wp.daoTable = " rsk_trial_list A left join rsk_trial_data_ecs B "
        + " on A.batch_no =B.batch_no and A.id_p_seqno =B.id_p_seqno "
        + " left join rsk_trial_data_jcic C "
        + " on A.batch_no =C.batch_no and A.id_p_seqno =C.id_p_seqno ";
    wp.whereOrder = " order by A.query_date desc , A.risk_group Asc, A.id_no Asc ";
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
    String sql2 = " select " + " decode(substr(data_flag,1,1),'N','',substr(data_flag,1,1))||"
        + " decode(substr(data_flag,2,1),'N','',substr(data_flag,2,1))|| "
        + " decode(substr(data_flag,3,1),'N','',substr(data_flag,3,1))|| "
        + " decode(substr(data_flag,4,1),'N','',substr(data_flag,4,1))|| "
        + " decode(substr(data_flag,5,1),'N','',substr(data_flag,5,1))|| "
        + " decode(substr(data_flag,6,1),'N','',substr(data_flag,6,1)) as kk_data_flag "
        + " from rsk_gage_list " + " where 1=1 " + " and id_no = ? ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "close_flag", "1")) {
        wp.colSet(ii, "tt_close_flag", ".待覆核");
      } else if (wp.colEq(ii, "close_flag", "N")) {
        wp.colSet(ii, "tt_close_flag", ".覆審中");
      } else if (wp.colEq(ii, "close_flag", "Y")) {
        wp.colSet(ii, "tt_close_flag", ".已結案");
      }

      if (wp.colEq(ii, "ecs046", "1")) {
        wp.colSet(ii, "tt_ecs046", ".設質");
      } else if (wp.colEq(ii, "ecs046", "2")) {
        wp.colSet(ii, "tt_ecs046", ".保人");
      } else if (wp.colEq(ii, "ecs046", "3")) {
        wp.colSet(ii, "tt_ecs046", ".風險行");
      }

      if (wp.colEq(ii, "action_code", "0")) {
        wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".原額用卡");
      } else if (wp.colEq(ii, "action_code", "1")) {
        wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".調降額度-未降足額度者凍結");
      } else if (wp.colEq(ii, "action_code", "2")) {
        wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".調降額度-未降足額度者維護特指");
      } else if (wp.colEq(ii, "action_code", "3")) {
        wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".調整額度");
      } else if (wp.colEq(ii, "action_code", "4")) {
        wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".調降額度-卡戶凍結(個繳)");
      } else if (wp.colEq(ii, "action_code", "5")) {
        wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".調降額度-維護特指");
      } else if (wp.colEq(ii, "action_code", "6")) {
        wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".卡戶凍結[4]");
      } else if (wp.colEq(ii, "action_code", "7")) {
        wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".卡片維護特指");
      } else if (wp.colEq(ii, "action_code", "8")) {
        wp.colSet(ii, "tt_action_code", wp.colStr(ii, "action_code") + ".額度內用卡");
      } else {
        wp.colSet(ii, "tt_action_code", "");
      }

      String sql1 = " select " + " trial_remark||trial_remark2||trial_remark3 as trial_remark_60 "
          + " from rsk_trial_idno_log " + " where 1=1 " + " and batch_no = ? "
          + " and id_p_seqno in (select id_p_seqno from crd_idno where id_no = ? ) ";

      sqlSelect(sql1, new Object[] {wp.colStr(ii, "batch_no"), wp.colStr(ii, "id_no")});
      log("A:" + sqlRowNum);
      if (sqlRowNum > 0) {
        wp.colSet(ii, "trial_remark_60", sqlStr("trial_remark_60"));
        if (wp.colEmpty(ii, "trial_remark_30")) {
          wp.colSet(ii, "trial_remark_30", sqlStr("trial_remark_60"));
        }
      }


      sqlSelect(sql2, new Object[] {wp.colStr(ii, "id_no")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "data_flag", sqlStr("kk_data_flag"));
      }

    }
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(isBatchNo)) {
      isBatchNo = wp.itemStr("is_batch_no");
    }

    if (empty(isIdPSeqno)) {
      isIdPSeqno = wp.itemStr("is_id_p_seqno");
    }

    if (isIdPSeqno.length() < 10) {
      wp.listCount[0] = (int) wp.itemNum("list_cnt");
      alertMsg("這筆為最後一筆！");
      return;
    }

    String[] bb = new String[2];
    String[] pp = new String[2];

    bb[0] = isBatchNo;
    pp[0] = isIdPSeqno;

    bb = commString.token(bb, ",");
    pp = commString.token(pp, 10);
    batchNo = bb[1];
    idPSeqno = pp[1];
    wp.colSet("is_batch_no", bb[0]);
    wp.colSet("is_id_p_seqno", pp[0]);

    selectTab1();
    selectTab2();

  }

  void selectTab1() {
    daoTid = "A1_";
    wp.selectSQL = "" + " A.batch_no ," + " A.id_no ," + " A.id_p_seqno ,"
        + " uf_idno_name(A.id_p_seqno) as chi_name ," + " A.query_date ," + " A.risk_group ,"
        + " A.trial_type ," + " A.trial_date ," + " A.trial_user ," + " A.action_code ,"
        + " A.trial_remark ," + " A.adj_credit_limit_rate ," + " A.adj_credit_limit_reason ,"
        + " A.adj_credit_limit_remain ," + " A.block_reason ," + " A.action_date ,"
        + " A.close_flag ," + " A.close_user ," + " A.close_date ," + " hex(A.rowid) as rowid ,"
        + " A.apr_date ," + " A.apr_user ," + " A.block_reason5 ," + " A.spec_status ,"
        + " A.trial_remark ," + " A.trial_remark2 ," + " A.trial_remark3 ," + " A.loan_flag ,"
        + " B.trial_reason  ," + " '' as db_ecs044 ," + " '' as db_ecs045 ," + " '' as db_ecs046 ,"
        + " '' as db_ecs047 ," + " A.mod_seqno ";
    wp.daoTable = " rsk_trial_list A, rsk_trial_parm B ";
    wp.whereStr = " where 1=1 " + " and (A.batch_no = B.batch_no) " + sqlCol(batchNo, "A.batch_no")
        + sqlCol(idPSeqno, "A.id_p_seqno");
    pageSelect();

    if (sqlRowNum <= 0) {
      errmsg("查無資料");
      return;
    }

    if (wp.colEq("A1_close_flag", "N")) {
      wp.colSet("A1_tt_close_flag", ".覆審中");
    } else if (wp.colEq("A1_close_flag", "1")) {
      wp.colSet("A1_tt_close_flag", ".待覆核");
    } else if (wp.colEq("A1_close_flag", "Y")) {
      wp.colSet("A1_tt_close_flag", ".已結案");
    }

    if (wp.colEq("A1_trial_type", "1")) {
      wp.colSet("A1_tt_trial_type", "人工");
    } else if (wp.colEq("A1_trial_type", "2")) {
      wp.colSet("A1_tt_trial_type", "批次");
    }

    // --db_ecs
    String sql1 = " select " + " ecs044 , " + " ecs045 , " + " ecs046 , " + " ecs047 "
        + " from rsk_trial_data_ecs " + " where batch_no = ? " + " and id_p_seqno = ? ";

    sqlSelect(sql1, new Object[] {batchNo, idPSeqno});

    if (sqlRowNum <= 0) {
      wp.colSet("A1_db_ecs044", "");
      wp.colSet("A1_db_ecs045", "");
      wp.colSet("A1_db_ecs046", "");
      wp.colSet("A1_db_ecs047", "");
      return;
    }
    wp.colSet("A1_db_ecs044", sqlStr("ecs044"));
    wp.colSet("A1_db_ecs045", sqlStr("ecs045"));
    wp.colSet("A1_db_ecs046", sqlStr("ecs046"));
    wp.colSet("A1_db_ecs047", sqlStr("ecs047"));

    if (eqIgno(sqlStr("ecs046"), "1")) {
      wp.colSet("A1_tt_db_ecs046", ".設質");
    } else if (eqIgno(sqlStr("ecs046"), "2")) {
      wp.colSet("A1_tt_db_ecs046", ".保人");
    } else if (eqIgno(sqlStr("ecs046"), "3")) {
      wp.colSet("A1_tt_db_ecs046", ".風險行");
    }

    String sql2 = " select " + " decode(substr(data_flag,1,1),'N','',substr(data_flag,1,1))||"
        + " decode(substr(data_flag,2,1),'N','',substr(data_flag,2,1))|| "
        + " decode(substr(data_flag,3,1),'N','',substr(data_flag,3,1))|| "
        + " decode(substr(data_flag,4,1),'N','',substr(data_flag,4,1))|| "
        + " decode(substr(data_flag,5,1),'N','',substr(data_flag,5,1))|| "
        + " decode(substr(data_flag,6,1),'N','',substr(data_flag,6,1)) as kk_data_flag "
        + " from rsk_gage_list " + " where 1=1 " + " and id_no = ? ";
    sqlSelect(sql2, new Object[] {wp.colStr("id_no")});
    if (sqlRowNum <= 0) {
      wp.colSet("A1_db_ecs047", "");
    }
    wp.colSet("A1_db_ecs047", sqlStr("kk_data_flag"));
  }

  void selectTab2() {
    daoTid = "acno.";
    String sql1 = " select " + " A.acct_type ," + " A.acct_key ," + " A.line_of_credit_amt , "
        + " B.block_reason1 ," + " B.block_reason2 ," + " B.block_reason3 ," + " B.block_reason4 ,"
        + " B.block_reason5 ," + " A.acno_p_seqno ," + " A.id_p_seqno "
        + " from act_acno A join cca_card_acct B on A.acno_p_seqno =B.acno_p_seqno and B.debit_flag<>'Y' "
        + " where A.id_p_seqno = ? " + " and A.acno_flag in ('1','3') ";
    sqlSelect(sql1, new Object[] {idPSeqno});

    if (sqlRowNum <= 0) {
      wp.listCount[0] = 0;
      return;
    }

    int llNrow = 0;
    llNrow = sqlRowNum;

    for (int ii = 0; ii < llNrow; ii++) {

      if (ii < 9) {
        wp.colSet(ii, "ser_num", "0" + (ii + 1));
      } else {
        wp.colSet(ii, "ser_num", (ii + 1));
      }

      wp.colSet(ii, "A2_wk_acct_key",
          sqlStr(ii, "acno.acct_type") + "-" + sqlStr(ii, "acno.acct_key"));
      daoTid = "card.";
      String sql3 = " select " + " sum(decode(sup_flag,'0',1,0)) as card_cnt , "
          + " sum(decode(sup_flag,'1',1,0)) as sup_cnt " + " from crd_card "
          + " where acno_p_seqno = ? " + " and current_code in ('0','2') "
          + " and uf_nvl(oppost_date,'99991231')>=to_char(add_months(sysdate,-6),'yyyymmdd') ";
      sqlSelect(sql3, new Object[] {sqlStr(ii, "acno.acno_p_seqno")});

      daoTid = "trlg.";
      String sql2 = " select * " + " from rsk_trial_action_log " + " where batch_no = ? "
          + " and acno_p_seqno = ? ";
      sqlSelect(sql2, new Object[] {batchNo, sqlStr(ii, "acno.acno_p_seqno")});
      if (sqlRowNum <= 0) {

        wp.colSet(ii, "A2_action_code", "");
        wp.colSet(ii, "A2_credit_limit_bef", sqlStr(ii, "acno.line_of_credit_amt"));
        wp.colSet(ii, "A2_credit_limit_aft", sqlStr(ii, "acno.line_of_credit_amt"));
        // wp.col_set(ii,"A2_block_reason4",sql_ss("acno.block_reason4"));
        // wp.col_set(ii,"A2_block_reason5",sql_ss("acno.block_reason5"));
        wp.colSet(ii, "A2_card_curr_cnt", sqlStr("card.card_cnt"));
        wp.colSet(ii, "A2_sup_curr_cnt", sqlStr("card.sup_cnt"));
        wp.colSet(ii, "A2_block_reason_bef",
            sqlStr(ii, "acno.block_reason1") + sqlStr(ii, "acno.block_reason2")
                + sqlStr(ii, "acno.block_reason3") + sqlStr(ii, "acno.block_reason4")
                + sqlStr(ii, "acno.block_reason5"));
        wp.colSet(ii, "A2_block_reason_aft", "");
        wp.colSet(ii, "A2_spec_status", "");
      } else {

        wp.colSet(ii, "A2_action_code", sqlStr("trlg.action_code"));
        wp.colSet(ii, "A2_credit_limit_bef", sqlStr("trlg.credit_limit_bef"));
        wp.colSet(ii, "A2_credit_limit_aft", sqlStr("trlg.credit_limit_aft"));
        // wp.col_set(ii,"A2_block_reason4",sql_ss("trlg.block_reason4"));
        // wp.col_set(ii,"A2_block_reason5",sql_ss("trlg.block_reason5"));
        wp.colSet(ii, "A2_card_curr_cnt", sqlStr("trlg.card_curr_cnt"));
        wp.colSet(ii, "A2_sup_curr_cnt", sqlStr("trlg.sup_curr_cnt"));
        wp.colSet(ii, "A2_block_reason_bef", sqlStr("trlg.block_reason_bef"));
        wp.colSet(ii, "A2_block_reason_aft", sqlStr("trlg.block_reason_aft"));
        wp.colSet(ii, "A2_spec_status", sqlStr("trlg.spec_status"));
      }

      wp.colSet(ii, "A2_acno_p_seqno", sqlStr(ii, "acno.acno_p_seqno"));
      wp.colSet(ii, "A2_id_p_seqno", sqlStr(ii, "acno.id_p_seqno"));
      wp.colSet(ii, "A2_acct_type", sqlStr(ii, "acno.acct_type"));
    }
    wp.listCount[0] = llNrow;
    wp.colSet("list_cnt", "" + llNrow);
  }

  @Override
  public void saveFunc() throws Exception {
    String[] lsWkAcctKey = wp.itemBuff("A2_wk_acct_key");
    wp.listCount[0] = lsWkAcctKey.length;
    rskm02.Rskm1050Func func = new rskm02.Rskm1050Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    sqlCommit(rc);
    if (rc <= 0) {
      errmsg(func.getMsg());
    } else
      saveAfter(false);

  }

  @Override
  public void procFunc() throws Exception {
    String[] lsBatchNo = wp.itemBuff("batch_no");
    String[] lsIdPSeqno = wp.itemBuff("id_p_seqno");
    String[] lsOpt = wp.itemBuff("opt");
    wp.listCount[0] = lsBatchNo.length;

    for (int ii = 0; ii < lsBatchNo.length; ii++) {
      if (!checkBoxOptOn(ii, lsOpt))
        continue;
      if (empty(lsBatchNo[ii]) || empty(lsIdPSeqno[ii]))
        continue;
      isBatchNo += lsBatchNo[ii] + ",";
      isIdPSeqno += lsIdPSeqno[ii];
    }

    if (isBatchNo.length() <= 0) {
      alertErr2("請選擇覆審資料");
      return;
    }
    dataRead();
  }

  public void updateProc() throws Exception {
    int llOk = 0, llErr = 0;
    rskm02.Rskm1050Func func = new rskm02.Rskm1050Func();
    func.setConn(wp);
    String lsActionCode = wp.itemStr("A1_action_code");
    String lsBlockReason4 = wp.itemStr("A1_block_reason");
    String lsBlockReason5 = wp.itemStr("A1_block_reason5");
    String lsSpecStatus = wp.itemStr("A1_spec_status");
    String[] lsWkAcctKey = wp.itemBuff("A2_wk_acct_key");
    String[] lsCreditLimitBef = wp.itemBuff("A2_credit_limit_bef");
    String[] lsCreditLimitAft = wp.itemBuff("A2_credit_limit_aft");

    String[] lsCardCurrCnt = wp.itemBuff("A2_card_curr_cnt");
    String[] lsSupCurrCnt = wp.itemBuff("A2_sup_curr_cnt");
    String[] lsBlockReasonBef = wp.itemBuff("A2_block_reason_bef");
    String[] lsBlockReasonAft = wp.itemBuff("A2_block_reason_aft");
    String[] lsPSeqno = wp.itemBuff("A2_acno_p_seqno");
    String[] lsAcctType = wp.itemBuff("A2_acct_type");
    wp.listCount[0] = lsWkAcctKey.length;
    rc = func.dbUpdate();
    if (rc <= 0) {
      dbRollback();
      errmsg(func.getMsg());
      return;
    }

    if (func.deleteLog() <= 0) {
      dbRollback();
      errmsg(func.getMsg());
      return;
    }

    for (int ii = 0; ii < lsWkAcctKey.length; ii++) {
      func.varsSet("wk_acct_key", lsWkAcctKey[ii]);
      func.varsSet("action_code", lsActionCode);
      func.varsSet("credit_limit_bef", lsCreditLimitBef[ii]);
      func.varsSet("credit_limit_aft", lsCreditLimitAft[ii]);
      func.varsSet("block_reason4", lsBlockReason4);
      func.varsSet("block_reason5", lsBlockReason5);
      func.varsSet("spec_status", lsSpecStatus);
      func.varsSet("card_curr_cnt", lsCardCurrCnt[ii]);
      func.varsSet("sup_curr_cnt", lsSupCurrCnt[ii]);
      func.varsSet("block_reason_bef", lsBlockReasonBef[ii]);
      func.varsSet("block_reason_aft", lsBlockReasonAft[ii]);
      func.varsSet("acno_p_seqno", lsPSeqno[ii]);
      func.varsSet("acct_type", lsAcctType[ii]);
      if (func.procList() > 0) {
        llOk++;
        continue;
      } else {
        llErr++;
        break;
      }
    }

    if (llErr > 0) {
      this.dbRollback();
      errmsg(func.getMsg());
      return;
    } else {
      this.sqlCommit(1);
      for (int ll = 0; ll < lsWkAcctKey.length; ll++) {
        wp.colSet(ll, "A2_action_code", wp.itemStr("A1_action_code"));
        wp.colSet(ll, "A2_block_reason4", wp.itemStr("A1_block_reason"));
        wp.colSet(ll, "A2_block_reason5", wp.itemStr("A1_block_reason5"));
        wp.colSet(ll, "A2_spec_status", wp.itemStr("A1_spec_status"));
      }
      alertMsg("存檔完成");
    }

  }

  void dataRead2() throws Exception {
    batchNo = wp.itemStr("A1_batch_no");
    idPSeqno = wp.itemStr("A1_id_p_seqno");

    if (empty(batchNo))
      batchNo = wp.itemStr("batch_no");
    if (empty(idPSeqno))
      idPSeqno = wp.itemStr("id_p_seqno");
    // --本行
    wp.selectSQL = " * ";
    wp.daoTable = " rsk_trial_data_ecs ";
    wp.whereStr = " where 1=1 and batch_no = ? and id_p_seqno = ? ";
    setString2(1, batchNo);
    setString(idPSeqno);
    pageSelect();
    if (sqlNotFind()) {
      selectOK();
    }

    // --JCIC
    wp.selectSQL = " * ";
    wp.daoTable = " rsk_trial_data_jcic ";
    wp.whereStr = " where 1=1 and batch_no = ? and id_p_seqno = ? ";
    setString2(1, batchNo);
    setString(idPSeqno);
    pageSelect();
    if (sqlNotFind()) {
      selectOK();
    }

    if (wp.colNum("ecs_jcic_score") == 0) {
      wp.colSet("ecs_jcic_score", wp.colNum("ecs004") + wp.colNum("tol_score"));
    }
    // --評等
    selectLevel();
    // --中文
    ttData();
  }

  void selectLevel() {
    String sql1 = " select " + " ecs_jcic_level " + " from rsk_trial_list " + " where batch_no = ? "
        + " and id_p_seqno = ? ";
    sqlSelect(sql1, new Object[] {batchNo, idPSeqno});
    if (sqlRowNum <= 0) {
      return;
    }
    wp.colSet("ecs_jcic_level", sqlStr("ecs_jcic_level"));
  }

  void ttData() {
    if (wp.colEq("ecs005", "1")) {
      wp.colSet("tt_ecs005", ".男");
    } else if (wp.colEq("ecs005", "2")) {
      wp.colSet("tt_ecs005", ".女");
    }

    if (wp.colEq("ecs006", "1")) {
      wp.colSet("tt_ecs006", ".博士");
    } else if (wp.colEq("ecs006", "2")) {
      wp.colSet("tt_ecs006", ".碩士");
    } else if (wp.colEq("ecs006", "3")) {
      wp.colSet("tt_ecs006", ".大學");
    } else if (wp.colEq("ecs006", "4")) {
      wp.colSet("tt_ecs006", ".專科");
    } else if (wp.colEq("ecs006", "5")) {
      wp.colSet("tt_ecs006", ".高中高職");
    } else if (wp.colEq("ecs006", "6")) {
      wp.colSet("tt_ecs006", ".其他");
    }

    if (wp.colEq("ecs046", "0")) {
      wp.colSet("tt_ecs046", ".無");
    } else if (wp.colEq("ecs046", "1")) {
      wp.colSet("tt_ecs046", ".設質");
    } else if (wp.colEq("ecs046", "2")) {
      wp.colSet("tt_ecs046", ".保人");
    } else if (wp.colEq("ecs046", "3")) {
      wp.colSet("tt_ecs046", ".風險行");
    }

    if (wp.colEq("jcic036", "A")) {
      wp.colSet("tt_jcic036", ".無註記");
    } else if (wp.colEq("jcic036", "B")) {
      wp.colSet("tt_jcic036", ".非信用疑慮註記");
    } else if (wp.colEq("jcic036", "C")) {
      wp.colSet("tt_jcic036", ".有信用疑慮註記");
    }

  }

  @Override
  public void initButton() {
    btnModeAud("XX");

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  @Override
  public void pdfPrint() throws Exception {
    // --永調
    wp.reportId = "rskm1050";
    TarokoPDF pdf = new TarokoPDF();
    // if(this.check_approve_zz()==false){
    // pdf = null;
    // return;
    // }
    wp.fileMode = "Y";
    pdf.pageVert = true;
    pdf.excelTemplate = "rskm1050_chg.xlsx";
    pdf.pageCount = 30;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;
  }

  void printData() throws Exception {
    int llCnt = 0;
    String lsPSeqno = "";
    String[] lsIdPSeqno = wp.itemBuff("id_p_seqno");
    String[] lsIdNo = wp.itemBuff("id_no");
    String[] lsQuryDate = wp.itemBuff("query_date");
    String[] lsBatchNo = wp.itemBuff("batch_no");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = lsIdNo.length;

    // --
    String sql0 = " select " + " A.query_date , " + " A.risk_group " + " from rsk_trial_list A "
    // + " left join rsk_trial_data_ecs B "
    // + " on A.batch_no =B.batch_no and A.id_p_seqno =B.id_p_seqno "
    // + " left join rsk_trial_data_jcic C "
    // + " on A.batch_no =C.batch_no and A.id_p_seqno =C.id_p_seqno "
        + " where A.query_date < ? " + " and A.id_p_seqno = ? " + " order by A.query_date desc "
        // + " , A.risk_group Asc, A.id_no desc "
        + commSqlStr.rownum(1);
    // --crd_idno
    String sql1 = " select " + " chi_name , " + " birthday , " + " asset_value " + " from crd_idno "
        + " where id_no = ? ";

    // --acct_type
    String sql2 = " select " + " acct_type , " + " acno_p_seqno " + " from act_acno "
        + " where acno_p_seqno in "
        + " (select acno_p_seqno from crd_card where id_p_seqno = ? and current_code ='0') "
        + " order by acct_type ";

    // --rsk_gage_list
    String sql3 =
        " select " + " decode(substr(data_flag,1,1),'N','',substr(data_flag,1,1)) as data_flag1 ,"
            + " decode(substr(data_flag,2,1),'N','',substr(data_flag,2,1)) as data_flag2 ,"
            + " decode(substr(data_flag,3,1),'N','',substr(data_flag,3,1)) as data_flag3 ,"
            + " decode(substr(data_flag,4,1),'N','',substr(data_flag,4,1)) as data_flag4 ,"
            + " decode(substr(data_flag,5,1),'N','',substr(data_flag,5,1)) as data_flag5 ,"
            + " decode(substr(data_flag,6,1),'N','',substr(data_flag,6,1)) as data_flag6  "
            + " from rsk_gage_list " + " where id_no = ? ";

    // --line_of_credit_amt
    String sql4 = " select " + " sum(line_of_credit_amt) as li_line_of_credit_amt "
        + " from act_acno " + " where acno_flag in ('1','3') "
        + " and acno_p_seqno in (select acno_p_seqno from crd_card where id_p_seqno =?) ";

    // --cca_card_acct
    String sql5 = " select " + " spec_status , " + " block_reason1 , " + " block_reason2 , "
        + " block_reason3 , " + " block_reason4 , " + " block_reason5 " + " from cca_card_acct "
        + " where debit_flag <> 'Y' " + " and acno_p_seqno in "
        + " (select acno_p_seqno from crd_card where id_p_seqno = ? and current_code ='0') ";

    // --rsk_trial_data_jcic
    String sql6 = " select " + " jcic031 , " + " jcic032 , " + " jcic034 , " + " jcic018_02 , "
        + " jcic036 , " + " jcic012_03 , " + " jcic008_02 , " + " jcic012_01 , " + " jcic012_05 , "
        + " jcic039 ," + " jcic033 ," + " jcic040 " + " from rsk_trial_data_jcic "
        + " where batch_no = ? " + " and id_p_seqno = ? ";

    // --select act_acno
    String sql8 = " select " + " line_of_credit_amt_cash , " + " payment_rate1 , "
        + " payment_rate2 , " + " payment_rate3 , " + " payment_rate4 , " + " payment_rate5 , "
        + " payment_rate6 , " + " payment_rate7 , " + " payment_rate8 , " + " payment_rate9 , "
        + " payment_rate10 , " + " payment_rate11 , " + " payment_rate12 , " + " curr_pd_rating , "
        + " class_code " + " from act_acno " + " where acno_p_seqno = ? ";

    // --select act_acno -1

    // --是否最近六個月內經本行覆審而遭調降額度或凍結*
    String sql9 = " select " + " count(*) as db_cnt " + " from rsk_acnolog " + " where "
        + " log_date between to_char(current date - 180 days ,'yyyymmdd') and to_char(sysdate,'yyyymmdd') "
        + " and (log_type ='1' and log_reason in ('V','W') or log_type ='3' "
        + " and (block_reason in ('0G','G1') or block_reason2 in ('0G','G1') or block_reason3 in ('0G','G1') "
        + " or block_reason4 in ('0G','G1') or block_reason5 in ('0G','G1') )) "
        + " and id_p_seqno = ? ";

    // --本行信用卡是否近6個月曾調降額度且調降理由碼為「3」(RC轉LOAN或分期調降) *
    String sql10 = " select " + " count(*) as db_cnt2 " + " from rsk_acnolog "
        + " where id_p_seqno = ? " + " and kind_flag ='A' "
        + " and log_date between to_char(current date -180 days ,'yyyymmdd') and to_char(sysdate,'yyyymmdd') "
        + " and log_type ='1' " + " and adj_loc_flag ='2' " + " and log_reason ='3' ";

    // --select min issue_date **
    String sql11 = " select " + " min(issue_date) as ls_issue_date " + " from crd_card "
        + " where id_p_seqno = ? ";

    // --保人
    String sql12 =
        " select " + " count(*) as db_cnt3 " + " from crd_rela " + " where rela_type = '1' "
            + " and id_p_seqno = ? " + " and end_date > to_char(sysdate,'yyyymmdd') ";

    // --近六個月交易金額
    String sql13 = " select " + " his_purchase_amt "
        + " from act_anal_sub A join act_acno B on A.p_seqno =B.p_seqno and B.acno_flag<>'Y'"
        + " where B.id_p_seqno = ? " + " order by A.acct_month DESC " + commSqlStr.rownum(12);

    // --sql14
    String sql14 = " select " + " max(trial_date) as ls_trial_date " + " from rsk_trial_idno "
        + " where id_p_seqno = ? ";

    // --sql15
    String sql15 = " select " + " risk_group " + " from rsk_trial_list " + " where batch_no = ? "
        + " and id_p_seqno = ? ";

    // --sql16
    String sql16 = " select " + " tran_type " + " from act_jrnl " + " where p_seqno = ? "
        + " order by acct_date desc " + commSqlStr.rownum(1);

    // --sql17 洗防
    String sql17 = " select " + " B.risk_level "
        + " from crd_idno A left join crd_idno_ext B on A.id_p_seqno = B.id_p_seqno "
        + " where A.id_no = ? ";

    int rr = -1;
    for (int ii = 0; ii < lsIdNo.length; ii++) {
      if (checkBoxOptOn(ii, aaOpt) == false)
        continue;
      lsPSeqno = "";
      llCnt++;
      rr++;
      // --畫面上有的
      log("id_no:" + lsIdNo[ii]);
      wp.colSet(rr, "wk_idno", lsIdNo[ii]);
      wp.colSet(rr, "wk_batch_no", lsBatchNo[ii]);
      wp.colSet(rr, "wk_query_date", lsQuryDate[ii]);
      // -sql0
      sqlSelect(sql0, new Object[] {lsQuryDate[ii], lsIdPSeqno[ii]});
      if (sqlRowNum > 0) {
        wp.colSet(rr, "wk_query_date_bef", sqlStr("query_date"));
        wp.colSet(rr, "wk_risk_group_1", sqlStr("risk_group"));
      }
      // --sql1
      sqlSelect(sql1, new Object[] {lsIdNo[ii]});
      if (sqlRowNum > 0) {
        wp.colSet(rr, "wk_chi_name", sqlStr("chi_name"));
        wp.colSet(rr, "wk_birth_day", sqlStr("birthday"));
        wp.colSet(rr, "wk_age", "" + (commString.strToInt(commString.mid(getSysDate(), 0, 4))
            - commString.strToInt(commString.mid(sqlStr("birthday"), 0, 4))));
        if (sqlNum("asset_value") > 0) {
          wp.colSet(rr, "wk_asset_value", "是");
        } else {
          wp.colSet(rr, "wk_asset_value", "否");
        }
      }

      // --sql2
      sqlSelect(sql2, new Object[] {lsIdPSeqno[ii]});
      int a1 = 0;
      String lsAcctType = "";
      a1 = sqlRowNum;
      if (a1 > 0) {
        for (int ll2 = 0; ll2 < a1; ll2++) {
          if (ll2 == 0) {
            lsAcctType += sqlStr(ll2, "acct_type");
            lsPSeqno = sqlStr("acno_p_seqno");
          } else {
            lsAcctType += "," + sqlStr(ll2, "acct_type");
          }
        }
        wp.colSet(rr, "wk_acct_type", lsAcctType);
      }

      // --sql3
      sqlSelect(sql3, new Object[] {lsIdNo[ii]});
      if (sqlRowNum > 0) {
        String lsDataFlag = "";
        if (!empty(sqlStr("data_flag1"))) {
          if (lsDataFlag.length() <= 0)
            lsDataFlag += sqlStr("data_flag1");
          else
            lsDataFlag += "," + sqlStr("data_flag1");
        }
        if (!empty(sqlStr("data_flag2"))) {
          if (lsDataFlag.length() <= 0)
            lsDataFlag += sqlStr("data_flag2");
          else
            lsDataFlag += "," + sqlStr("data_flag2");
        }
        if (!empty(sqlStr("data_flag3"))) {
          if (lsDataFlag.length() <= 0)
            lsDataFlag += sqlStr("data_flag3");
          else
            lsDataFlag += "," + sqlStr("data_flag3");
        }
        if (!empty(sqlStr("data_flag4"))) {
          if (lsDataFlag.length() <= 0)
            lsDataFlag += sqlStr("data_flag4");
          else
            lsDataFlag += "," + sqlStr("data_flag4");
        }
        if (!empty(sqlStr("data_flag5"))) {
          if (lsDataFlag.length() <= 0)
            lsDataFlag += sqlStr("data_flag5");
          else
            lsDataFlag += "," + sqlStr("data_flag5");
        }
        if (!empty(sqlStr("data_flag6"))) {
          if (lsDataFlag.length() <= 0)
            lsDataFlag += sqlStr("data_flag6");
          else
            lsDataFlag += "," + sqlStr("data_flag6");
        }
        wp.colSet(rr, "wk_data_flag", lsDataFlag);
      }

      // --sql4
      sqlSelect(sql4, new Object[] {lsIdPSeqno[ii]});
      if (sqlRowNum > 0) {
        wp.colSet(rr, "wk_line_of_credit_amt", "" + sqlNum("li_line_of_credit_amt"));
      }

      // --sql5
      sqlSelect(sql5, new Object[] {lsIdPSeqno[ii]});
      if (sqlRowNum > 0) {
        String lsBlockReason = "";
        wp.colSet(rr, "wk_spec_status", sqlStr("spec_status"));
        if (empty(sqlStr("block_reason1")) && empty(sqlStr("block_reason2"))
            && empty(sqlStr("block_reason3")) && empty(sqlStr("block_reason4"))
            && empty(sqlStr("block_reason5"))) {
          wp.colSet(rr, "wk_block_reason", "否");
        } else {
          lsBlockReason = sqlStr("block_reason1") + "|" + sqlStr("block_reason2") + "|"
              + sqlStr("block_reason3") + "|" + sqlStr("block_reason4") + "|"
              + sqlStr("block_reason5");
          if (pos(lsBlockReason, "61") > 0 || pos(lsBlockReason, "71") > 0
              || pos(lsBlockReason, "72") > 0 || pos(lsBlockReason, "73") > 0
              || pos(lsBlockReason, "81") > 0) {
            wp.colSet(rr, "wk_block_reason", "否");
          } else {
            wp.colSet(rr, "wk_block_reason", "是");
          }
        }
      }

      // --sql6
      sqlSelect(sql6, new Object[] {lsBatchNo[ii], lsIdPSeqno[ii]});
      if (sqlRowNum > 0) {
        if (eqIgno(sqlStr("jcic031"), "Y") || eqIgno(sqlStr("jcic032"), "Y")) {
          wp.colSet(rr, "wk_jcic31_32", "是");
        } else {
          wp.colSet(rr, "wk_jcic31_32", "否");
        }
        if (eqIgno(sqlStr("jcic034"), "Y")) {
          wp.colSet(rr, "wk_jcic34", "是");
        } else {
          wp.colSet(rr, "wk_jcic34", "否");
        }
        if (sqlNum("jcic018_02") > 0) {
          wp.colSet(rr, "wk_jcic018_02", "是");
        } else {
          wp.colSet(rr, "wk_jcic018_02", "否");
        }
        if (eqIgno(sqlStr("jcic036"), "C")) {
          wp.colSet(rr, "wk_jcic036", "是");
        } else {
          wp.colSet(rr, "wk_jcic036", "否");
        }
        if (sqlNum("jcic012_03") > 0 && sqlNum("jcic008_02") > 90) {
          wp.colSet(rr, "wk_jcic012_08", "是");
        } else {
          wp.colSet(rr, "wk_jcic012_08", "否");
        }
        if (eqIgno(sqlStr("jcic012_01"), "Y")) {
          wp.colSet(rr, "wk_jcic012_01", "是");
        } else {
          wp.colSet(rr, "wk_jcic012_01", "否");
        }
        if (sqlNum("jcic012_03") > 1) {
          wp.colSet(rr, "wk_jcic012_03", "是");
        } else {
          wp.colSet(rr, "wk_jcic012_03", "否");
        }
        if (sqlNum("jcic012_05") > 1) {
          wp.colSet(rr, "wk_jcic012_05", "是");
        } else {
          wp.colSet(rr, "wk_jcic012_05", "否");
        }
        wp.colSet(rr, "wk_jcic039", sqlNum("jcic039"));
        wp.colSet(rr, "wk_jcic033", "" + sqlInt("jcic033"));
        wp.colSet(rr, "wk_jcic040", sqlNum("jcic040"));
        if (sqlNum("jcic040") >= 22) {
          wp.colSet(rr, "wk_jcic040_22", "是");
        } else {
          wp.colSet(rr, "wk_jcic040_22", "否");
        }
      }

      // --sql8
      if (!empty(lsPSeqno)) {
        sqlSelect(sql8, new Object[] {lsPSeqno});
        if (sqlRowNum > 0) {
          if (sqlNum("line_of_credit_amt_cash") == 0) {
            wp.colSet(rr, "wk_line_of_amt_cash", "是");
          } else {
            wp.colSet(rr, "wk_line_of_amt_cash", "否");
          }
          String lsPaymentRate = "";
          lsPaymentRate += sqlStr("payment_rate1");
          lsPaymentRate += " 、 " + sqlStr("payment_rate2");
          lsPaymentRate += " 、 " + sqlStr("payment_rate3");
          lsPaymentRate += " 、 " + sqlStr("payment_rate4");
          lsPaymentRate += " 、 " + sqlStr("payment_rate5");
          lsPaymentRate += " 、 " + sqlStr("payment_rate6");
          lsPaymentRate += " 、 " + sqlStr("payment_rate7");
          lsPaymentRate += " 、 " + sqlStr("payment_rate8");
          lsPaymentRate += " 、 " + sqlStr("payment_rate9");
          lsPaymentRate += " 、 " + sqlStr("payment_rate10");
          lsPaymentRate += " 、 " + sqlStr("payment_rate11");
          lsPaymentRate += " 、 " + sqlStr("payment_rate12");
          wp.colSet(rr, "wk_payment_rate", lsPaymentRate);
          wp.colSet(rr, "wk_pd_rating", sqlStr("curr_pd_rating"));
          wp.colSet(rr, "wk_class_code", sqlStr("class_code"));
        }
      }

      // --sql9
      sqlSelect(sql9, new Object[] {lsIdPSeqno[ii]});
      if (sqlRowNum > 0) {
        if (sqlNum("db_cnt") > 0) {
          wp.colSet(rr, "wk_log_ct", "是");
        } else {
          wp.colSet(rr, "wk_log_ct", "否");
        }
      }

      // --sql10
      sqlSelect(sql10, new Object[] {lsIdPSeqno[ii]});
      if (sqlRowNum > 0) {
        if (sqlNum("db_cnt2") > 0) {
          wp.colSet(rr, "wk_log_ct2", "是");
        } else {
          wp.colSet(rr, "wk_log_ct2", "否");
        }
      }

      // --sql11
      sqlSelect(sql11, new Object[] {lsIdPSeqno[ii]});
      if (sqlRowNum > 0) {
        int lsEarlyDate = 0;
        lsEarlyDate = commDate.yearsBetween(this.getSysDate(), sqlStr("ls_issue_date"));
        wp.colSet(rr, "wk_issue_date", "" + lsEarlyDate);
      }

      // --sql12
      sqlSelect(sql12, new Object[] {lsIdPSeqno[ii]});
      if (sqlRowNum > 0) {
        if (sqlNum("db_cnt3") > 0) {
          wp.colSet(rr, "wk_rela_type", "是");
        } else {
          wp.colSet(rr, "wk_rela_type", "否");
        }
      }

      // --sql13
      sqlSelect(sql13, new Object[] {lsIdPSeqno[ii]});
      if (sqlRowNum > 0) {
        String lsPurchaseAmt6 = "";

        lsPurchaseAmt6 += "1. "
            + commString.numFormat(Math.round(sqlNum(0, "his_purchase_amt") / 10000), "#,##0") + "萬元 "
            + "2. " + commString.numFormat(Math.round(sqlNum(1, "his_purchase_amt") / 10000), "#,##0")
            + "萬元 " + "3. "
            + commString.numFormat(Math.round(sqlNum(2, "his_purchase_amt") / 10000), "#,##0") + "萬元 "
            + "4. " + commString.numFormat(Math.round(sqlNum(3, "his_purchase_amt") / 10000), "#,##0")
            + "萬元 " + "5. "
            + commString.numFormat(Math.round(sqlNum(4, "his_purchase_amt") / 10000), "#,##0") + "萬元 "
            + "6. " + commString.numFormat(Math.round(sqlNum(5, "his_purchase_amt") / 10000), "#,##0")
            + "萬元 ";
        wp.colSet(ii, "wk_purchase_amt", lsPurchaseAmt6);

        int lsPurchaseAmt12 = 0;
        for (int pa = 0; pa < 12; pa++) {
          lsPurchaseAmt12 += sqlNum(pa, "his_purchase_amt");
        }
        wp.colSet(ii, "wk_sum_purchase", "" + lsPurchaseAmt12);
      }

      // --sql14
      sqlSelect(sql14, new Object[] {lsIdPSeqno[ii]});
      if (sqlRowNum > 0) {
        wp.colSet(rr, "wk_trial_date", sqlStr("ls_trial_date"));
      }

      // --sql15
      sqlSelect(sql15, new Object[] {lsBatchNo[ii], lsIdPSeqno[ii]});
      if (sqlRowNum > 0) {
        wp.colSet(rr, "wk_risk_group", sqlStr("risk_group"));
      }

      // --sql16
      sqlSelect(sql16, new Object[] {lsPSeqno});
      if (sqlRowNum > 0) {
        if (eqIgno(sqlStr("tran_type"), "AUT1")) {
          wp.colSet(rr, "wk_tran_type", "■本行自扣　□他行自扣　□其他");
        } else if (eqIgno(sqlStr("tran_type"), "ACH1")) {
          wp.colSet(rr, "wk_tran_type", "□本行自扣　■他行自扣　□其他");
        } else {
          wp.colSet(rr, "wk_tran_type", "□本行自扣　□他行自扣　■其他");
        }
      }

      // --sql17
      sqlSelect(sql17, new Object[] {lsIdNo[ii]});
      if (sqlRowNum > 0) {
        if (eqIgno(sqlStr("risk_level"), "H")) {
          wp.colSet(rr, "ex5D", "■為洗錢高風險等級之卡戶");
        } else {
          wp.colSet(rr, "ex5D", "□為洗錢高風險等級之卡戶");
        }
      } else
        wp.colSet(rr, "ex5D", "□為洗錢高風險等級之卡戶");
    }

    if (llCnt <= 0) {
      wp.respHtml = "TarokoErrorPDF";
      return;
    }
    wp.listCount[0] = llCnt;
    pdfPrint();
  }

  public void pdfPrint1() throws Exception {
    // --ECS JCIC Detail
    if (this.checkApproveZz() == false) {
      wp.respHtml = "TarokoErrorPDF";
      return;
    }
    wp.reportId = "rskm1050";
    dataRead2();
    TarokoPDF pdf = new TarokoPDF();
    wp.fileMode = "Y";
    pdf.pageVert = true;
    pdf.excelTemplate = "rskm1050_jcic.xlsx";
    pdf.pageCount = 35;
    pdf.sheetNo = 0;
    pdf.procesPDFreport(wp);
    pdf = null;
    return;
  }

}
