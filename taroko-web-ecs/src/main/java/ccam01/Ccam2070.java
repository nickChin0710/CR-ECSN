package ccam01;
/** DEBIT卡臨時調整額度維護
 * 19-1220:    Alex  fix approve
 * 19-1210:    Alex  add initButton
 * 19-0611:    JH    p_seqno >>acno_p_xxx
 * V.2018-0504-JH
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * 109-12-30  V1.00.01  shiyuqi       修改无意义命名                                                                                     *
 * */
import java.util.Arrays;

import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Ccam2070 extends BaseAction {
  taroko.base.CommDate commDate = new taroko.base.CommDate();
  Ccam2070Func func;
  ofcapp.EcsApprove ooAppr = null;
  String cardAcctIdx = "", kk2 = "";

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
    } else if (eqIgno(wp.buttonCode, "R2")) {
      // -臨調-
      f5CalcAmt();

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
      setRskm0930Data();
      initPage();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    }
  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_idno")) && empty(wp.itemStr("ex_card_no"))) {
      alertErr2("卡號  身分證字號 : 不可同時空白 !");
      return;
    }

    String lsIdno = wp.itemStr("ex_idno");
    if (!empty(lsIdno)) {
      if (lsIdno.length() != 8 && lsIdno.length() != 10) {
        alertErr2("身分證字號 為 8碼 or 10碼");
        return;
      }
    }


    // wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    // wp.pageControl();

    wp.sqlCmd = " select " + " A.acct_type," + " A.acno_p_seqno," + " A.debit_flag ,"
        + " A.acno_flag ," + " A.card_acct_idx ," + " A.block_status ," + " A.spec_status ,"
        + " A.adj_quota ," + " A.adj_eff_start_date ," + " A.adj_eff_end_date ," + " A.adj_area ,"
        + " A.tot_amt_month ," + " A.adj_inst_pct ," + " A.adj_remark ,"
        + " uf_acno_key2(A.acno_p_seqno,A.debit_flag) as acct_key , " + " B.acct_no , "
        + " A.adj_reason , "
        + " (select sys_data1 from cca_sys_parm3 where sys_key =A.adj_reason and sys_id='ADJREASON' ) as tt_adj_reason , "
        + " A.adj_inst_pct , " + " A.mod_user , " + " to_char(A.mod_time,'yyyymmdd') as mod_date "
        + " from cca_card_acct A , dbc_card B " + " where A.acno_p_seqno = B.p_seqno "
        + " and A.debit_flag ='Y' " + " and B.current_code = '0' ";
    if (wp.itemEmpty("ex_card_no") == false) {
      wp.sqlCmd += " and B.card_no =:kk_card_no";
    }
    String lsAcctKey = wp.itemStr2("ex_idno");
    if (lsAcctKey.length() == 8) {
      lsAcctKey += "000";
      wp.sqlCmd +=
          " and A.acno_p_seqno in (select p_seqno from dba_acno where acct_key = :kk_idno)";
    } else if (lsAcctKey.length() == 10) {
      wp.sqlCmd +=
          " and A.id_p_seqno in (select id_p_seqno from dbc_idno where id_no like :kk_idno)";
    }
    setString2("kk_card_no", wp.itemStr2("ex_card_no"));
    setString2("kk_idno", lsAcctKey);

    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    listWkdata(sqlRowNum);

  }

  void listWkdata(int aRow) {
    for (int ii = 0; ii < aRow; ii++) {
      String sql1 = "select " + " line_of_credit_amt ," + " acno_flag , "
          + " uf_vd_idno_name(id_p_seqno) as idno_name ,"
          + " uf_corp_name(corp_p_seqno) as corp_name " + " from dba_acno " + " where 1=1"
          + sqlCol(wp.colStr(ii, "acno_p_seqno"), "p_seqno");
      sqlSelect(sql1);
      if (sqlRowNum > 0) {
        wp.colSet(ii, "line_of_credit_amt", sqlStr("line_of_credit_amt"));
        if (eqIgno(sqlStr("acno_flag"), "Y")) {
          wp.colSet(ii, "acno_name", sqlStr("corp_name"));
        } else {
          wp.colSet(ii, "acno_name", sqlStr("idno_name"));
        }

        // wp.col_set(ii, "wk_acct_key", wp.col_ss(ii, "acct_type") + "-" + wp.col_ss(ii,
        // "acct_key"));
        wp.colSet(ii, "adj_eff_date", commString.strToYmd(wp.colStr(ii, "adj_eff_start_date")) + " -- "
            + commString.strToYmd(wp.colStr(ii, "adj_eff_end_date")));
      }

      String sql2 = " select " + " adj_eff_start_date , " + " adj_eff_end_date , " + " adj_area , "
          + " tot_amt_month , " + " adj_inst_pct , " + " adj_remark , " + " adj_quota"
          + " from cca_card_acct_t " + " where mod_type ='ADJ_LIMIT' "
          + col(toNum(wp.colStr(ii, "card_acct_idx")), "card_acct_idx",true);

      sqlSelect(sql2);

      if (sqlRowNum > 0) {
        wp.colSet(ii, "apr_flag", "N");
        wp.colSet(ii, "adj_eff_start_date", sqlStr("adj_eff_start_date"));
        wp.colSet(ii, "adj_eff_end_date", sqlStr("adj_eff_end_date"));
        wp.colSet(ii, "adj_area", sqlStr("adj_area"));
        wp.colSet(ii, "tot_amt_month", sqlStr("tot_amt_month"));
        wp.colSet(ii, "adj_inst_pct", sqlStr("adj_inst_pct"));
        wp.colSet(ii, "adj_remark", sqlStr("adj_remark"));
        wp.colSet(ii, "adj_eff_date", commString.strToYmd(sqlStr("adj_eff_start_date")) + " -- "
            + commString.strToYmd(sqlStr("adj_eff_end_date")));
        wp.colSet(ii, "adj_quota", sqlStr("adj_quota"));
      } else {
        wp.colSet(ii, "apr_flag", "Y");
      }

      String sql3 = "select nvl(no_connect_flag,'N') as connect_flag ,"
          + " cnt_amount as org_cnt_amt," + " day_amount as org_day_amt,"
          + " day_cnt    as org_day_cnt," + " month_amount as org_month_amt," + " withdraw_fee "
          + " from cca_debit_parm " + " where BIN_NO in ("
          + " select substr(card_no,1,6) from cca_card_base where card_acct_idx =? "
          + " ) or bin_no ='000000' " + " order by bin_no desc " + commSqlStr.rownum(1);

      daoTid = "AA.";
      setDouble(1, wp.colNum(ii, "card_acct_idx"));
      sqlSelect(sql3);
      if (sqlRowNum > 0) {
        double liRate = wp.colNum(ii, "tot_amt_month") / 100;
        if (liRate > 0) {
          wp.colSet(ii, "cur_cnt_amt", sqlNum("AA.org_cnt_amt") * liRate);
          wp.colSet(ii, "cur_day_amt", sqlNum("AA.org_day_amt") * liRate);
          wp.colSet(ii, "cur_day_cnt", sqlNum("AA.org_day_cnt"));
          wp.colSet(ii, "cur_month_amt", sqlNum("AA.org_month_amt") * liRate);
        }
      }
    }
  }

  @Override
  public void querySelect() throws Exception {
    cardAcctIdx = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(cardAcctIdx)) {
      cardAcctIdx = wp.itemStr("card_acct_idx");
    }

    wp.sqlCmd = "select hex(A.rowid) as rowid, A.mod_seqno," + " A.card_acct_idx," + " A.adj_area ,"
        + " A.adj_reason ," + " A.tot_amt_month ," + " A.tot_amt_month as ori_tot_amt_month ,"
        + " A.adj_remark," + " A.adj_eff_start_date  as adj_eff_date1,"
        + " A.adj_eff_end_date    as adj_eff_date2, " + " A.adj_eff_start_date  as ori_eff_date1,"
        + " A.adj_eff_end_date    as ori_eff_date2, " + " A.adj_date , " + " A.adj_user , "
        + " B.line_of_credit_amt, " + " B.acct_no, " + " B.inst_auth_loc_amt,"
        + " B.p_seqno, B.id_p_seqno , " + " to_char(A.mod_time,'yyyymmdd') as mod_date , "
        + " A.mod_user " + " from cca_card_acct A join dba_acno B on A.acno_p_seqno =B.p_seqno "
        + " where A.debit_flag ='Y' " + sqlCol(cardAcctIdx, "A.card_acct_idx");
    pageSelect();
    if (sqlRowNum <= 0) {
      alertErr("查無持卡人[授權帳戶(cca_card_acct)]資料");
      return;
    }

    // --
    if (wp.colEmpty("adj_eff_date1") && wp.colEmpty("add_eff_date2")) {
      String lsSysdate = commDate.sysDate();
      wp.colSet("adj_eff_date1", lsSysdate);
      wp.colSet("adj_eff_date2", commDate.dateAdd(lsSysdate, 0, 1, 0));
      wp.colSet("adj_date", "");
      wp.colSet("adj_user", "");
    } else {
      wp.colSet("chg", "Y");
    }

    selectCcaDebitParm();
    checkRelaFlag();

    selectDetlData();
    selectCardNo();
  }

  void selectCardNo() {
    String sql1 = " select " + " card_no " + " from dbc_card " + " where id_p_seqno = ? "
        + " order by current_code  ";
    sqlSelect(sql1, new Object[] {wp.colStr("id_p_seqno")});

    if (sqlRowNum <= 0)
      return;
    wp.colSet("card_no", sqlStr("card_no"));
  }

  void selectCcaDebitParm() {

    String sql1 = "select nvl(no_connect_flag,'N') as connect_flag ,"
        + " cnt_amount as org_cnt_amt," + " day_amount as org_day_amt,"
        + " day_cnt    as org_day_cnt," + " month_amount as org_month_amt," + " withdraw_fee "
        + " from cca_debit_parm " + " where BIN_NO in ("
        + " select substr(card_no,1,6) from cca_card_base where card_acct_idx =? "
        + " ) or bin_no ='000000' " + " order by bin_no desc " + commSqlStr.rownum(1);

    daoTid = "AA.";
    setDouble(1, wp.colNum("card_acct_idx"));
    sqlSelect(sql1);
    if (sqlRowNum < 0) {
      alertErr2("cca_debit_parm.Select error");
      return;
    }
    // -調整前-
    wp.colSet("org_cnt_amt", sqlStr("AA.org_cnt_amt"));
    wp.colSet("org_day_amt", sqlStr("AA.org_day_amt"));
    wp.colSet("org_day_cnt", sqlStr("AA.org_day_cnt"));
    wp.colSet("org_month_amt", sqlStr("AA.org_month_amt"));
    // -調整後-
    double liRate = wp.colNum("tot_amt_month") / 100;
    if (liRate > 0) {
      wp.colSet("cur_cnt_amt", sqlNum("AA.org_cnt_amt") * liRate);
      wp.colSet("cur_day_amt", sqlNum("AA.org_day_amt") * liRate);
      wp.colSet("cur_day_cnt", Math.floor(sqlNum("AA.org_day_cnt") * liRate));
//      wp.colSet("cur_day_cnt", sqlNum("AA.org_day_cnt"));
      wp.colSet("cur_month_amt", sqlNum("AA.org_month_amt") * liRate);
    }
  }

  void checkRelaFlag() {
    String sql1 = " select " + " B.fh_flag , " + " B.non_asset_balance , " + " A.id_no , "
        + " A.chi_name , " + " A.asset_value "
        + " from dbc_idno A left join crd_correlate B on A.id_no =B.correlate_id "
        + " where A.id_p_seqno =? " + " order by B.crt_date desc " + commSqlStr.rownum(1);

    sqlSelect(sql1, new Object[] {wp.colStr("id_p_seqno")});
    if (sqlRowNum <= 0) {
      return;
    }

    wp.colSet("id_no", sqlStr("id_no"));
    wp.colSet("idno_name", sqlStr("chi_name"));
    if (eqIgno(sqlStr("fh_flag"), "Y")) {
      wp.colSet("rela_flag", "Y");
    } else {
      wp.colSet("rela_flag", "N");
    }
    wp.colSet("bond_amt", "" + sqlNum("asset_value"));
    wp.colSet("asset_balance", "" + sqlNum("non_asset_balance"));
  }

  void selectDetlData() throws Exception {
    wp.pageRows = 999;
    wp.selectSQL =
        " risk_type , " + " 100 as cnt_amt_pct , " + " 100 as ori_cnt_amt_pct , " + " cnt_amt , "
            + " cnt_amt as ori_cnt_amt , " + " 100 as day_amt_pct , " + " 100 as ori_day_amt_pct , "
            + " day_amt , " + " day_amt as ori_day_amt , " + " 100 as month_amt_pct , "
            + " 100 as ori_month_amt_pct , " + " month_amt , " + " month_amt as ori_month_amt , "
            + " 100 as day_cnt_pct , " + " day_cnt , " + " day_cnt as ori_day_cnt , "
            + " 100 as month_cnt_pct , " + " month_cnt , " + " month_cnt as ori_month_cnt ";

    wp.daoTable = " cca_debit_parm2 ";
    wp.whereStr = " where 1=1 ";
    wp.whereOrder = " order by risk_type ";

    pageQuery();

    if (sqlNotFind()) {
      selectOK();
      return;
    }
    int ilParm2Cnt = 0;
    ilParm2Cnt = wp.selectCnt;
    wp.setListCount(0);

    String sql1 = " select " + " * " + " from cca_debit_adj_parm " + " where 1=1 "
        + " and card_acct_idx = ? ";

    sqlSelect(sql1, new Object[] {wp.colStr("card_acct_idx")});

    if (sqlRowNum <= 0)
      return;
    int ilParmCnt = 0;
    ilParmCnt = sqlRowNum;
    for (int ii = 0; ii < ilParmCnt; ii++) {
      for (int ll = 0; ll < ilParm2Cnt; ll++) {
        if (!eqIgno(sqlStr(ii, "risk_type"), wp.colStr(ll, "risk_type")))
          continue;

        wp.colSet(ll, "cnt_amt_pct", "" + sqlInt(ii, "cnt_amt_pct"));
        wp.colSet(ll, "day_amt_pct", "" + sqlInt(ii, "day_amt_pct"));
        wp.colSet(ll, "day_cnt_pct", "" + sqlInt(ii, "day_cnt_pct"));
        wp.colSet(ll, "month_amt_pct", "" + sqlInt(ii, "month_amt_pct"));
        wp.colSet(ll, "month_cnt_pct", "" + sqlInt(ii, "month_cnt_pct"));

        wp.colSet(ll, "ori_cnt_amt_pct", "" + sqlInt(ii, "cnt_amt_pct"));
        wp.colSet(ll, "ori_day_amt_pct", "" + sqlInt(ii, "day_amt_pct"));
        wp.colSet(ll, "ori_month_amt_pct", "" + sqlInt(ii, "month_amt_pct"));

        wp.colSet(ll, "cnt_amt",
            "" + (int) (sqlNum(ii, "cnt_amt_pct") * wp.colNum(ll, "cnt_amt") / 100));
        wp.colSet(ll, "day_amt",
            "" + (int) (sqlNum(ii, "day_amt_pct") * wp.colNum(ll, "day_amt") / 100));
        wp.colSet(ll, "month_amt",
            "" + (int) (sqlNum(ii, "month_amt_pct") * wp.colNum(ll, "month_amt") / 100));
        
        wp.colSet(ll, "day_cnt",
        	"" + (int) (sqlNum(ii,"day_cnt_pct") * wp.colNum(ll,"day_cnt") / 100));
        
        wp.colSet(ll, "month_cnt",
            	"" + (int) (sqlNum(ii,"month_cnt_pct") * wp.colNum(ll,"month_cnt") / 100));
        

      }
    }
  }

  int listCheck() {
    int ii = wp.itemRows("risk_type");
    wp.listCount[0] = ii; // aa_rt.length;
    wp.colSet("IND_NUM", ii);
    if (!isUpdate())
      return 1;

    // -check 重覆-
    String[] aaRt = wp.itemBuff("risk_type");
    String[] aaOpt = wp.itemBuff("opt");
    int llErr = 0;

    // String[] aa_pct =wp.item_buff("cnt_amt_pct");
    // String[] aa_amt =wp.item_buff("db_cnt_amt");
    // -check duplication-
    ii = -1;
    for (String ss : aaRt) {
      ii++;

      // wp.ddd("%s. pct[%s], amt[%s]",ii,wp.sss("cnt_amt_pct-"+ii),wp.sss("db_cnt_amt-"+ii));
      wp.colSet(ii, "ok_flag", "");
      // -option-ON-
      if (checkBoxOptOn(ii, aaOpt)) {
        aaRt[ii] = "";
        continue;
      }

      if (ii != Arrays.asList(aaRt).indexOf(ss)) {
        wp.colSet(ii, "ok_flag", "!"); // -重覆-
        llErr++;
      }
    }
    if (llErr > 0) {
      alertErr("資料值重複: " + llErr);
      return -1;
    }

    // err_alert("JJJ-text");
    return rc;
  }

  @Override
  public void saveFunc() throws Exception {
    if (eqIgno(wp.respHtml, "ccam2070_detl")) {
      // -keep-list-
      listCheck();
      if (rc != 1)
        return;

      ooAppr = new ofcapp.EcsApprove(wp);

      if (!wp.itemEq("adj_eff_date1", wp.itemStr("ori_eff_date1"))
          || !wp.itemEq("adj_eff_date2", wp.itemStr("ori_eff_date2"))
          || wp.itemNum("tot_amt_month") > wp.itemNum("ori_tot_amt_month")) {
        if (ooAppr.adjLimitApprove(wp.modPgm(), wp.itemStr("approval_user"),
            wp.itemStr("approval_passwd"), wp.itemNum("tot_amt_month")) != 1) {
          alertErr2(ooAppr.getMesg());
          return;
        }
      }

      int llOk = 0, llErr = 0;
      func = new ccam01.Ccam2070Func();
      func.setConn(wp);
      rc = func.dbSave(strAction);
      if (rc != 1) {
        sqlCommit(-1);
        alertErr2(func.getMsg());
        return;
      }

      if (isDelete()) {
        sqlCommit(1);
        wp.listCount[0] = 0;
        clearFunc();
        alertMsg("刪除完成");
        return;
      }

      // -list-update--------------
      String[] aaRt = wp.itemBuff("risk_type");


      // -delete no-approve-
      if (func.dbDeleteAdjParm() < 0) {
        alertErr(func.getMsg());
        return;
      }

      for (int ll = 0; ll < aaRt.length; ll++) {
        wp.colSet(ll, "ok_flag", "");
        if (empty(aaRt[ll])) {
          continue;
        }

        String liCntAmtPct = wp.itemStr(ll, "cnt_amt_pct");
        String liDayAmtPct = wp.itemStr(ll, "day_amt_pct");
        String liDayCntPct = wp.itemStr(ll, "day_cnt_pct");
        String liMonthAmtPct = wp.itemStr(ll, "month_amt_pct");
        String liMonthCntPct = wp.itemStr(ll, "month_cnt_pct");

        func.varsSet("risk_type", aaRt[ll]);
        func.varsSet("cnt_amt_pct", liCntAmtPct);
        func.varsSet("day_amt_pct", liDayAmtPct);
        func.varsSet("day_cnt_pct", liDayCntPct);
        func.varsSet("month_amt_pct", liMonthAmtPct);
        func.varsSet("month_cnt_pct", liMonthCntPct);
        if (func.dbInsertAdjParm() == 1) {
          llOk++;
        } else {
          llErr++;
        }
      }

      this.sqlCommit(rc);
      alertMsg("資料存檔處理完成; OK=" + llOk + ", ERR=" + llErr);
      return;
    } else if (eqIgno(wp.respHtml, "ccam2070_detl2")) {
      rskm02.Rskm0940Func func = new rskm02.Rskm0940Func();
      func.setConn(wp);

      rc = func.dbSave(strAction);
      sqlCommit(rc);
      if (rc != 1) {
        alertErr2(func.getMsg());
      } else
        this.saveAfter(false);
    }

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "ccam2070_detl")) {
      this.btnModeAud("XX");
    }

  }

  @Override
  public void initPage() {
    wp.colSet("ind_num", "0");
    wp.colSet("ex_id_code", "0");
    if (eqIgno(wp.respHtml, "ccam2070_detl2")) {
      int liTimeAmt = 0, liDdAmt = 0, liMmAmt = 0, liDdCnt = 0, liMmCnt = 0;
      wp.colSet("time_lmt", "40000");
      wp.colSet("dd_lmt", "60000");
      wp.colSet("mm_lmt", "500000");
      wp.colSet("time_pcnt", "100");
      wp.colSet("dd_pcnt", "100");
      wp.colSet("mm_pcnt", "100");
      wp.colSet("dd_cnt_pcnt", "100");
      wp.colSet("mm_cnt_pcnt", "100");

      liTimeAmt = 40000 * 100 / 100;
      liDdAmt = 60000 * 100 / 100;
      liMmAmt = 500000 * 100 / 100;
      liDdCnt = 99 * 100 / 100;
      liMmCnt = 999 * 100 / 100;

      wp.colSet("wk_time_amt", "" + liTimeAmt);
      wp.colSet("wk_dd_amt", "" + liDdAmt);
      wp.colSet("wk_mm_amt", "" + liMmAmt);
      wp.colSet("wk_dd_cnt", "" + liDdCnt);
      wp.colSet("wk_mm_cnt", "" + liMmCnt);

      wp.colSet("tel_user", wp.loginUser);
      wp.colSet("tel_date", getSysDate());
      wp.colSet("tel_time", commDate.sysTime());
      wp.colSet("area_code", "國內外");
      wp.colSet("adj_date1", getSysDate());
      wp.colSet("user_no", wp.loginUser);
      userName();

    }

  }

  void userName() {
    String sql1 = " select " + " usr_cname " + " from sec_user " + " where usr_id = ? ";

    sqlSelect(sql1, new Object[] {wp.loginUser});

    if (sqlRowNum > 0) {
      wp.colSet("user_name", sqlStr("usr_cname"));
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "Ccam2070_detl")) {
        wp.optionKey = wp.colStr("adj_reason");
        dddwList("dddw_adj_reason", "cca_sys_parm3", "sys_key", "sys_data1",
            "where sys_id='ADJREASON'");

        wp.optionKey = wp.colStr("ex_risk_type");
        dddwList("dddw_risk_type", "cca_debit_parm2", "risk_type", "uf_tt_risk_type(risk_type)",
            "where 1=1");

      }
    } catch (Exception ex) {
    }
    try {
      if (eqIgno(wp.respHtml, "Ccam2070_detl2")) {
        wp.optionKey = wp.colStr(0, "charge_user");
        dddwList("dddw_charge_user", "ptr_sys_idtab", "wf_id", "wf_id",
            "where wf_type ='RSKM0930_CHARGE' ");
      }
    } catch (Exception ex) {
    }
  }

  void selectCcaDebitParm2(String riskType) throws Exception {
    wp.sqlCmd = "select " + " cnt_amt as wk_db_cnt_amt," + " day_amt as wk_db_day_amt, "
        + " month_amt as wk_db_month_amt," + " day_cnt as wk_db_day_cnt, "
        + " month_cnt as wk_db_month_cnt" + " from cca_debit_parm2  " + " where risk_type =:s1";
    this.setString("s1", riskType);

    this.sqlSelect();
    if (sqlRowNum <= 0) {
      alertErr2("查無卡號: 風險類別=" + riskType);
    }
    return;
  }

  public void wfAjaxRiskType(TarokoCommon wr) throws Exception {
    super.wp = wr;

    // String ls_winid =
    selectCcaDebitParm2(wp.itemStr("ax_risk_type"));
    if (rc != 1) {
      return;
    }
    wp.addJSON("wk_db_cnt_amt", sqlStr("wk_db_cnt_amt"));
    wp.addJSON("wk_db_day_amt", sqlStr("wk_db_day_amt"));
    wp.addJSON("wk_db_month_amt", sqlStr("wk_db_month_amt"));
    wp.addJSON("wk_db_day_cnt", sqlStr("wk_db_day_cnt"));
    wp.addJSON("wk_db_month_cnt", sqlStr("wk_db_month_cnt"));

  }

  // --轉匯簽單
  void setRskm0930Data() {
    wp.colSet("id_no", wp.itemStr("id_no"));
    wp.colSet("card_no", wp.itemStr("card_no"));
    wp.colSet("chi_name", wp.itemStr("idno_name"));
    String sql1 = "select e_mail_addr from dbc_idno where id_no = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("id_no")});
    if (sqlRowNum > 0) {
      wp.colSet("h_mail", sqlStr("e_mail_addr"));
    }
  }

  void f5CalcAmt() {
    if (wp.itemNum("tot_amt_month") <= 0) {
      alertErr2("臨調放大比率 不可小於等於 0");
      return;
    }
    int liAmtMonth = 0;
    liAmtMonth = (int) wp.itemNum("tot_amt_month");
    String[] liOriCntAmtPct = wp.itemBuff("ori_cnt_amt_pct");
    String[] liOriDayAmtPct = wp.itemBuff("ori_day_amt_pct");
    String[] liOriMonthAmtPct = wp.itemBuff("ori_month_amt_pct");

    wp.colSet("cur_day_cnt", Math.floor(wp.itemNum("org_day_cnt") * liAmtMonth / 100));
//    wp.colSet("cur_day_cnt", wp.itemNum("org_day_cnt"));
    wp.colSet("cur_month_amt", Math.round(wp.itemNum("org_month_amt") * liAmtMonth / 100));
    wp.colSet("cur_day_amt", Math.round(wp.itemNum("org_day_amt") * liAmtMonth / 100));
    wp.colSet("cur_cnt_amt", Math.round(wp.itemNum("org_cnt_amt") * liAmtMonth / 100));

    wp.listCount[0] = wp.itemRows("risk_type");

    for (int ii = 0; ii < wp.itemRows("risk_type"); ii++) {

      wp.colSet(ii, "cnt_amt_pct", "" + liAmtMonth);
      wp.colSet(ii, "day_amt_pct", "" + liAmtMonth);
      wp.colSet(ii, "month_amt_pct", "" + liAmtMonth);

      // wp.col_set(ii,"cnt_amt_pct", ""+commString.ss_2Int(li_ori_cnt_amt_pct[ii])*li_amt_month/100);
      // wp.col_set(ii,"day_amt_pct", ""+commString.ss_2Int(li_ori_day_amt_pct[ii])*li_amt_month/100);
      // wp.col_set(ii,"month_amt_pct",
      // ""+commString.ss_2Int(li_ori_month_amt_pct[ii])*li_amt_month/100);

      wp.colSet(ii, "cnt_amt",
          "" + (int) (wp.colNum(ii, "cnt_amt_pct") * wp.colNum(ii, "ori_cnt_amt") / 100));
      wp.colSet(ii, "day_amt",
          "" + (int) (wp.colNum(ii, "day_amt_pct") * wp.colNum(ii, "ori_day_amt") / 100));
      wp.colSet(ii, "month_amt",
          "" + (int) (wp.colNum(ii, "month_amt_pct") * wp.colNum(ii, "ori_month_amt") / 100));
    }

  }

}
