/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *   
******************************************************************************/
package rskm02;
/**
 * 2019-1230   JH    credit_limit: busi.func >>ecsfunc
 * 2019-0619:  JH    p_xxx >>acno_pxxx
 *
 */

import busi.func.AcnoBlockReason;
import busi.func.AcnoCreditLimit;
import ofcapp.BaseAction;

public class Rskp1050 extends BaseAction {
  String batchNo = "", idPSeqno = "";
  String isBatchNo = "", isIdPSeqno = "", isErrorDesc = "";
  int logRow = 0;
  AcnoCreditLimit ACL = new AcnoCreditLimit();

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
      procRead();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "rskp1050")) {
        wp.optionKey = wp.colStr("ex_action_code");
        ddlbList("dddw_trial_action", wp.colStr("ex_action_code"),
            "ecsfunc.DeCodeRsk.trial_action");
      }

      if (eqIgno(wp.respHtml, "rskp1050")) {
        wp.optionKey = wp.colStr("ex_risk_group");
        dddwList("dddw_risk_group", "rsk_trial_parm2", "risk_group", "risk_group",
            "where 1=1 and apr_flag = 'Y' order by risk_group");
      }

    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_query_date1"), wp.itemStr("ex_query_date2")) == false) {
      alertErr2("查詢日期: 起迄錯誤");
      return;
    }

    if (this.chkStrend(wp.itemStr("ex_trial_date1"), wp.itemStr("ex_trial_date2")) == false) {
      alertErr2("覆審日期: 起迄錯誤");
      return;
    }

    String lsWhere = " where 1=1 and A.close_flag ='1' "
        + sqlCol(wp.itemStr("ex_query_date1"), "A.query_date", ">=")
        + sqlCol(wp.itemStr("ex_query_date2"), "A.query_date", "<=")
        + sqlCol(wp.itemStr("ex_trial_date1"), "A.trial_date", ">=")
        + sqlCol(wp.itemStr("ex_trial_date2"), "A.trial_date", "<=")
        + sqlCol(wp.itemStr("ex_batch_no"), "A.batch_no")
        + sqlCol(wp.itemStr("ex_action_code"), "A.action_code")
        + sqlCol(wp.itemStr("ex_trial_user"), "A.trial_user", "like%")
        + sqlCol(wp.itemStr("ex_risk_group"), "A.risk_group")
        + sqlCol(wp.itemStr("ex_close_flag"), "A.close_flag");

    if (!wp.itemEmpty("ex_idno")) {
      lsWhere += " and A.id_p_seqno in (select id_p_seqno from crd_idno where id_no ='"
          + wp.itemStr("ex_idno") + "')";
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "" + " A.risk_group ," + " A.id_no ," + " A.batch_no ," + " A.id_p_seqno ,"
        + " uf_idno_name(A.id_p_seqno) as chi_name ," + " A.query_date ," + " A.action_code ,"
        + " A.trial_remark||A.trial_remark2||A.trial_remark3 as trial_remark ,"
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
    wp.whereOrder = " order by risk_group Asc, A.id_no desc ";
    pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfter();
    wp.setListCount(1);
    wp.setPageValue();
  }

  void queryAfter() throws Exception {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_block_4_5",
          wp.colStr(ii, "block_reason") + "/" + wp.colStr(ii, "block_reason5"));
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
      }

      String sql1 = " select " + " trial_remark||trial_remark2||trial_remark3 as trial_remark_60 "
          + " from rsk_trial_idno_log " + " where 1=1 " + " and batch_no = ? "
          + " and id_p_seqno in (select id_p_seqno from crd_idno where id_no = ? ) ";

      sqlSelect(sql1, new Object[] {wp.colStr(ii, "batch_no"), wp.colStr(ii, "id_no")});

      if (sqlRowNum > 0) {
        wp.colSet(ii, "trial_remark_60", sqlStr("trial_remark_60"));
        if (wp.colEmpty(ii, "trial_remark_30")) {
          wp.colSet(ii, "trial_remark_30", sqlStr("trial_remark_60"));
        }
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

  @Override
  public void saveFunc() throws Exception {
    String[] lsAcctKey = wp.itemBuff("A2_wk_acct_key");
    wp.listCount[0] = lsAcctKey.length;

    rskm02.Rskp1050Func func = new rskm02.Rskp1050Func();
    func.setConn(wp);
    func.varsSet("batch_no", wp.itemStr2("A1_batch_no"));
    func.varsSet("id_p_seqno", wp.itemStr2("A1_id_p_seqno"));
    rc = func.dataProc();
    sqlCommit(rc);
    if (rc != 1) {
      errmsg(func.getMsg());
      return;
    }
    //
    // if(selectActionLog(wp.item_ss("A1_batch_no"),wp.item_ss("A1_id_p_seqno"))<0) return;
    // selectList(wp.item_ss("A1_batch_no"),wp.item_ss("A1_id_p_seqno"));
    // for(int rr=0;rr<log_row;rr++){
    // if(wf_update_block_reason(rr)<0){
    // wp.col_set("ok_flag", "X");
    // this.dbRollback();
    // continue;
    // }
    // ddd("action_code:"+sql_ss(rr,"trli.action_code"));
    // if(wf_update_credit_limit(sql_ss(rr,"trli.action_code"),rr)<0){
    // wp.col_set("ok_flag", "X");
    // this.dbRollback();
    // continue;
    // }
    // }
    //
    // if(eq_igno(wp.col_ss("ok_flag"),"X")) return ;
    alertMsg("覆核成功");
  }

  public void procRead() throws Exception {
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

  @Override
  public void procFunc() throws Exception {
    int llOk = 0, llErr = 0;
    rskm02.Rskp1050Func func = new rskm02.Rskp1050Func();
    func.setConn(wp);

    // String[] ls_batch_no = wp.item_buff("batch_no");
    // String[] ls_id_p_seqno = wp.item_buff("id_p_seqno");
    // String[] ls_close_flag = wp.item_buff("close_flag");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = wp.itemRows("batch_no");
    if (optToIndex(aaOpt[0]) < 0) {
      alertErr2("未點選欲覆核資料");
      return;
    }
    optNumKeep(wp.listCount[0], aaOpt);

    for (int ii = 0; ii < aaOpt.length; ii++) {
      rc = 1;
      int rr = optToIndex(aaOpt[ii]);
      if (rr < 0)
        continue;

      if (!eqAny(wp.itemStr(rr, "close_flag"), "1"))
        continue;

      String lsBatchNo = wp.itemStr(rr, "batch_no");
      String lsIdPseqno = wp.itemStr(rr, "id_p_seqno");
      func.varsSet("batch_no", lsBatchNo);
      func.varsSet("id_p_seqno", lsIdPseqno);

      optOkflag(rr);
      if (func.dataProc() <= 0) {
        llErr++;
        wp.colSet(rr, "err_msg", func.getMsg());
        rc = -1;
        // errmsg(func.getMsg());
      } else {
        llOk++;
      }
      sqlCommit(rc);
      optOkflag(rr, rc);
    }

    if (func.iiCombo > 0) {
      ecsfunc.EcsCallbatch batch = new ecsfunc.EcsCallbatch(wp);
      rc = batch.callBatch("cms_a002");
      if (rc != 1) {
        alertErr2("callBatch error; " + batch.getMesg());
        return;
      } else {
        alertMsg("callBatch OK; Batch-seqno=" + batch.batchSeqno());
      }
    }

    alertMsg("覆核完成     成功:" + llOk + " 失敗:" + llErr);
  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  void dataRead2() throws Exception {
    batchNo = wp.itemStr("A1_batch_no");
    idPSeqno = wp.itemStr("A1_id_p_seqno");

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

    ttData();

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

    // --評分
    String sql1 = " select " + " ecs_jcic_level " + " from rsk_trial_list " + " where batch_no = ? "
        + " and id_p_seqno = ? ";
    sqlSelect(sql1, new Object[] {batchNo, idPSeqno});
    if (sqlRowNum <= 0) {
      return;
    }
    wp.colSet("ecs_jcic_level", sqlStr("ecs_jcic_level"));


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
    wp.whereStr =
        " where A.batch_no = B.batch_no " + sqlCol(batchNo, "A.batch_no") + sqlCol(idPSeqno, "A.id_p_seqno");
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

    if (wp.colEq("A1_loan_flag", "0")) {
      wp.colSet("A1_tt_loan_flag", ".正常");
    } else if (wp.colEq("A1_loan_flag", "1")) {
      wp.colSet("A1_tt_loan_flag", ".加強催理");
    }

    if (wp.colEq("A1_trial_type", "1")) {
      wp.colSet("A1_tt_trial_type", "人工");
    } else if (wp.colEq("A1_trial_type", "2")) {
      wp.colSet("A1_tt_trial_type", "批次");
    }

    if (wp.colEq("A1_action_code", "0")) {
      wp.colSet("A1_tt_action_code", wp.colStr("A1_action_code") + ".原額用卡");
    } else if (wp.colEq("A1_action_code", "1")) {
      wp.colSet("A1_tt_action_code", wp.colStr("A1_action_code") + ".調降額度-未降足額度者凍結");
    } else if (wp.colEq("A1_action_code", "2")) {
      wp.colSet("A1_tt_action_code", wp.colStr("A1_action_code") + ".調降額度-未降足額度者維護特指");
    } else if (wp.colEq("A1_action_code", "3")) {
      wp.colSet("A1_tt_action_code", wp.colStr("A1_action_code") + ".調整額度");
    } else if (wp.colEq("A1_action_code", "4")) {
      wp.colSet("A1_tt_action_code", wp.colStr("A1_action_code") + ".調降額度-卡戶凍結(個繳)");
    } else if (wp.colEq("A1_action_code", "5")) {
      wp.colSet("A1_tt_action_code", wp.colStr("A1_action_code") + ".調降額度-維護特指");
    } else if (wp.colEq("A1_action_code", "6")) {
      wp.colSet("A1_tt_action_code", wp.colStr("A1_action_code") + ".卡戶凍結[4]");
    } else if (wp.colEq("A1_action_code", "7")) {
      wp.colSet("A1_tt_action_code", wp.colStr("A1_action_code") + ".卡片維護特指");
    } else if (wp.colEq("A1_action_code", "8")) {
      wp.colSet("A1_tt_action_code", wp.colStr("A1_action_code") + ".額度內用卡");
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
    }
    wp.colSet("A1_db_ecs044", sqlStr("ecs044"));
    wp.colSet("A1_db_ecs045", sqlStr("ecs045"));
    wp.colSet("A1_db_ecs046", sqlStr("ecs046"));
    wp.colSet("A1_db_ecs047", sqlStr("ecs047"));

    if (eqIgno(sqlStr("ecs046"), "1")) {
      wp.colSet("A1_tt_db_ecs046", sqlStr("ecs046") + ".設質");
    } else if (eqIgno(sqlStr("ecs046"), "2")) {
      wp.colSet("A1_tt_db_ecs046", sqlStr("ecs046") + ".保人");
    } else if (eqIgno(sqlStr("ecs046"), "3")) {
      wp.colSet("A1_tt_db_ecs046", sqlStr("ecs046") + ".風險行");
    }

    if (!wp.colEmpty("A1_adj_credit_limit_reason")) {
      String sql2 = " select " + " wf_desc " + " from ptr_sys_idtab "
          + " where wf_type ='ADJ_REASON_DOWN' " + " and wf_id = ? ";

      sqlSelect(sql2, new Object[] {wp.colStr("A1_adj_credit_limit_reason")});

      if (sqlRowNum > 0) {
        wp.colSet("A1_tt_adj_credit_limit_reason",
            wp.colStr("A1_adj_credit_limit_reason") + "." + sqlStr("wf_desc"));
      }
    }

  }

  void selectTab2() {
    daoTid = "acno.";
    String sql1 = " select " + " A.acct_type ," + " A.acct_key ," + " A.line_of_credit_amt , "
        + " B.block_reason1 ," + " B.block_reason2 ," + " B.block_reason3 ," + " B.block_reason4 ,"
        + " B.block_reason5 ," + " A.acno_p_seqno ," + " A.id_p_seqno "
        + " from act_acno A join cca_card_acct B on A.acno_p_seqno =B.acno_p_seqno and B.debit_flag<>'Y' "
        + " where A.id_p_seqno = ? " + " and A.acno_flag in ('1','3') ";
    sqlSelect(sql1, new Object[] {idPSeqno});
    log("sql_nrow: " + sqlRowNum);
    if (sqlRowNum <= 0) {
      wp.listCount[0] = 0;
      return;
    }

    int llNrow = 0;
    llNrow = sqlRowNum;

    for (int ii = 0; ii < llNrow; ii++) {
      wp.colSet(ii, "A2_wk_acct_key",
          sqlStr(ii, "acno.acct_type") + "-" + sqlStr(ii, "acno.acct_key"));
      daoTid = "card.";
      String sql3 = " select " + " sum(decode(sup_flag,'0',1,0)) as card_cnt , "
          + " sum(decode(sup_flag,'1',1,0)) as sup_cnt " + " from crd_card "
          + " where acno_p_seqno = ? " + " and current_code in ('0','2') "
          + " and uf_nvl(oppost_date,'99991231')>=to_char(add_months(sysdate,-6),'yyyymmdd') ";
      sqlSelect(sql3, new Object[] {sqlStr(ii, "acno.p_seqno")});

      daoTid = "trlg.";
      String sql2 = " select * " + " from rsk_trial_action_log " + " where batch_no = ? "
          + " and id_p_seqno = ? ";
      sqlSelect(sql2, new Object[] {batchNo, idPSeqno});
      if (sqlRowNum <= 0) {
        wp.colSet(ii, "A2_action_code", "");
        wp.colSet(ii, "A2_credit_limit_bef", sqlStr("acno.line_of_credit_amt"));
        wp.colSet(ii, "A2_credit_limit_aft", sqlStr("acno.line_of_credit_amt"));
        wp.colSet(ii, "A2_block_reason4", sqlStr("acno.block_reason4"));
        wp.colSet(ii, "A2_block_reason5", sqlStr("acno.block_reason5"));
        wp.colSet(ii, "A2_card_curr_cnt", sqlStr("card.card_cnt"));
        wp.colSet(ii, "A2_sup_curr_cnt", sqlStr("card.sup_cnt"));
        wp.colSet(ii, "A2_block_reason_bef",
            sqlStr("acno.block_reason1") + sqlStr("acno.block_reason2")
                + sqlStr("acno.block_reason3") + sqlStr("acno.block_reason4")
                + sqlStr("acno.block_reason5"));
        wp.colSet(ii, "A2_block_reason_aft", "");
      } else {
        wp.colSet(ii, "A2_action_code", sqlStr("trlg.action_code"));
        wp.colSet(ii, "A2_credit_limit_bef", sqlStr("trlg.credit_limit_bef"));
        wp.colSet(ii, "A2_credit_limit_aft", sqlStr("trlg.credit_limit_aft"));
        wp.colSet(ii, "A2_block_reason4", sqlStr("trlg.block_reason4"));
        wp.colSet(ii, "A2_block_reason5", sqlStr("trlg.block_reason5"));
        wp.colSet(ii, "A2_spec_status", sqlStr("trlg.spec_status"));
        wp.colSet(ii, "A2_card_curr_cnt", sqlStr("trlg.card_curr_cnt"));
        wp.colSet(ii, "A2_sup_curr_cnt", sqlStr("trlg.sup_curr_cnt"));
        wp.colSet(ii, "A2_block_reason_bef", sqlStr("trlg.block_reason_bef"));
        wp.colSet(ii, "A2_block_reason_aft", sqlStr("trlg.block_reason_aft"));
      }

      wp.colSet(ii, "A2_acno_p_seqno", sqlStr("acno.acno_p_seqno"));
      wp.colSet(ii, "A2_id_p_seqno", sqlStr("acno.id_p_seqno"));
      wp.colSet(ii, "A2_acct_type", sqlStr("acno.acct_type"));


      if (wp.colEq(ii, "A2_action_code", "0")) {
        wp.colSet(ii, "A2_tt_action_code", wp.colStr(ii, "A2_action_code") + ".原額用卡");
      } else if (wp.colEq(ii, "A2_action_code", "1")) {
        wp.colSet(ii, "A2_tt_action_code", wp.colStr(ii, "A2_action_code") + ".調降額度-未降足額度者凍結");
      } else if (wp.colEq(ii, "A2_action_code", "2")) {
        wp.colSet(ii, "A2_tt_action_code", wp.colStr(ii, "A2_action_code") + ".調降額度-未降足額度者維護特指");
      } else if (wp.colEq(ii, "A2_action_code", "3")) {
        wp.colSet(ii, "A2_tt_action_code", wp.colStr(ii, "A2_action_code") + ".調整額度");
      } else if (wp.colEq(ii, "A2_action_code", "4")) {
        wp.colSet(ii, "A2_tt_action_code", wp.colStr(ii, "A2_action_code") + ".調降額度-卡戶凍結(個繳)");
      } else if (wp.colEq(ii, "A2_action_code", "5")) {
        wp.colSet(ii, "A2_tt_action_code", wp.colStr(ii, "A2_action_code") + ".調降額度-維護特指");
      } else if (wp.colEq(ii, "A2_action_code", "6")) {
        wp.colSet(ii, "A2_tt_action_code", wp.colStr(ii, "A2_action_code") + ".卡戶凍結[4]");
      } else if (wp.colEq(ii, "A2_action_code", "7")) {
        wp.colSet(ii, "A2_tt_action_code", wp.colStr(ii, "A2_action_code") + ".卡片維護特指");
      } else if (wp.colEq(ii, "A2_action_code", "8")) {
        wp.colSet(ii, "A2_tt_action_code", wp.colStr(ii, "A2_action_code") + ".額度內用卡");
      }

    }
    wp.listCount[0] = llNrow;
    wp.colSet("list_cnt", "" + llNrow);
  }

  int selectActionLog(String lsBatchNo, String lsIdPSeqno) {
    daoTid = "trlog.";
    String sql1 = " select " + " * " + " from rsk_trial_action_log " + " where batch_no = ? "
        + " and id_p_seqno = ? ";
    sqlSelect(sql1, new Object[] {lsBatchNo, lsIdPSeqno});
    if (sqlRowNum <= 0)
      return -1;
    logRow = sqlRowNum;
    return 1;
  }
  // --

  void selectList(String lsBatchNo, String lsIdPSeqno) {
    daoTid = "trli.";
    String sql1 = " select " + " * " + " from rsk_trial_list " + " where batch_no = ? "
        + " and id_p_seqno = ? ";
    sqlSelect(sql1, new Object[] {lsBatchNo, lsIdPSeqno});
  }

  int wfUpdateBlockReason(int oo) {
    if (empty(sqlStr("trli.block_reason4")) && empty(sqlStr("trli.block_reason5"))
        && empty(sqlStr("trli.spec_status"))) {
      return 0;
    }

    AcnoBlockReason BR = new AcnoBlockReason();
    BR.setConn(wp);
    if (BR.trialActionBlock(sqlStr(oo, "trlog.acno_p_seqno"), sqlStr("trli.block_reason4"),
        sqlStr("trli.block_reason5"), sqlStr("trli.spec_status")) == -1) {
      errmsg(BR.getMsg());
      return -1;
    }
    return 0;
  }

  int wfUpdateCreditLimit(String lsActionCode, int oo) {
    // --降額
    if (pos("|1|2|3|4|5", lsActionCode) <= 0)
      return 0;

    if (sqlNum(oo, "trlog.credit_limit_before") == sqlNum(oo, "trlog.credit_limit_after"))
      return 0;


    ACL.setConn(wp);

    ACL.adjReason = sqlStr("trli.adj_credit_limit_reason");
    ACL.imAmtBefore = sqlNum(oo, "trlog.credit_limit_before");
    ACL.imAmtAfter = sqlNum(oo, "trlog.credit_limit_after");
    ACL.acnoPseqno = sqlStr(oo, "trlog.acno_p_seqno");

    if (ACL.trialAction() == -1) {
      errmsg("帳戶降額失敗: " + ACL.getMsg());
      return -1;
    }
    return 0;
  }

}
