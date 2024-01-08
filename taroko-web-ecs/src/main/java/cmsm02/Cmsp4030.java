package cmsm02;
/** 客服經辦D檔調整作業
 * 2020-0717   JustinWu ++ default_chk in initPage
 * 2020-0103:  Alex  sum_total , initPage
 * 2019-0614:  JH    p_xxx >>acno_pxxx
   2019-0422:  JH    mod_user<>apr_user
 * 2018-0726:	JH		modify
 * 109-04-27   shiyuqi       updated for project coding standard     
 * 111-11-23   machao   頁面bug調整， 覆核功能bug調整 *   
 * */
import ofcapp.BaseAction;

public class Cmsp4030 extends BaseAction {
  String lsIn = "";

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
    }
    // else if (eq_igno(wp.buttonCode, "A")) {
    // /* 新增功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "U")) {
    // /* 更新功能 */
    // saveFunc();
    // }
    // else if (eq_igno(wp.buttonCode, "D")) {
    // /* 刪除功能 */
    // saveFunc();
    // }
    else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C1")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "C2")) {
      // -資料處理-
      cancelFunc();
    }
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "cmsp4030")) {
        wp.optionKey = wp.colStr(0, "ex_curr_code");
        dddwList("dddw_dc_curr_code_tw", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type = 'DC_CURRENCY' order by id_code");
        // -----------
        wp.optionKey = wp.colStr(0, "ex_adj_dept");
        dddwList("dddw_dept_no", "ptr_dept_code", "dept_code", "dept_name",
            "where 1=1 order by dept_code");
        // ------------
        wp.optionKey = wp.colStr(0, "ex_user");
        dddwList("dddw_sec_user", "sec_user", "usr_id", "usr_cname", "where 1=1 ");
      }

    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (this.chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
      alertErr2("登錄日期起迄：輸入錯誤");
      return;
    }

    String lsWhere = " where nvl(acct_code,'')<>'DP' "
        + sqlCol(wp.itemStr("ex_crt_date1"), "crt_date", ">=")
        + sqlCol(wp.itemStr("ex_crt_date2"), "crt_date", "<=")
        + sqlCol(wp.itemStr("ex_user"), "crt_user") + sqlCol(wp.itemStr("ex_batch_no"), "appr_no");

    if (wp.itemEq("ex_apr_flag", "N")) {
      lsWhere += " and apr_date = '' ";
    } else {
      lsWhere += " and apr_date <> '' ";
    }

    // if (!empty(wp.item_ss("ex_batch_no"))) {
    // ls_where += sql_col(wp.item_ss("ex_batch_no"), "appr_no")
    // +" and apr_date <>''";
    // }
    // else {
    // ls_where += " and nvl(apr_date,'')='' ";
    // }

    boolean lbItem01 = wp.itemEq("ex_act_item1", "1");
    boolean lbItem02 = wp.itemEq("ex_act_item2", "1");
    boolean lbItem03 = wp.itemEq("ex_act_item3", "1");
    if (lbItem01) {
      lsIn += ",'AF','LF','CF','PF','SF','CC'";
    }
    if (lbItem02) {
      lsIn += ",'RI','AI','CI'";
    }
    if (lbItem03) {
      lsIn += ",'PN'";
    }
    if (!empty(lsIn)) {
      lsWhere += " and acct_code in (''" + lsIn + ")";
    }

    if (wp.itemEq("ex_debit_flag", "Y")) {
      lsWhere += " and nvl(debit_flag,'N') ='Y' ";
      if (!wp.itemEmpty("ex_idno")) {
        lsWhere += inAcnoDba("p_seqno", wp.itemStr2("ex_idno"), "like%");
      }
    } else {
      lsWhere += " and nvl(debit_flag,'N') <>'Y' ";
      if (!wp.itemEmpty("ex_idno")) {
        lsWhere += inAcnoAct("", wp.itemStr2("ex_idno"), "like%");
      }
    }

    if (!wp.itemEmpty("ex_curr_code")) {
//      lsWhere += " and nvl(curr_code,'901') ='" + wp.itemStr("ex_curr_code") + "'";
        lsWhere += sqlCol(wp.itemStr("ex_curr_code"), "nvl(curr_code,'901')");
    }

    if (!wp.itemEmpty("ex_adj_dept")) {
//      lsWhere += " and uf_nvl(adj_dept,'CS') ='" + wp.itemStr2("ex_adj_dept") + "'";
    	lsWhere += sqlCol(wp.itemStr2("ex_adj_dept"), "uf_nvl(adj_dept,'CS')");
    }

    
    sum(lsWhere);
    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
    sqlParm.clear();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "A.*," + " uf_curr_sort(curr_code) as db_curr_sort ,"
        + " uf_acno_key2(p_seqno,acct_type) as db_acct_key ," + " beg_bal as tw_beg_bal ,"
        + " end_bal as tw_end_bal ," + " d_avail_bal as tw_d_avail_bal ,"
        + " uf_dc_amt(curr_code,beg_bal,dc_beg_bal) as beg_bal ,"
        + " uf_dc_amt(curr_code,end_bal,dc_end_bal) as end_bal ,"
        + " uf_dc_amt(curr_code,d_avail_bal,dc_d_avail_bal) as d_avail_bal ,"
        + " uf_nvl(curr_code,'901') as curr_code ," + " uf_idno_name(card_no) as db_idno_name, "
        + " hex(rowid) as rowid ," + " uf_tt_acct_code(A.acct_code) as tt_acct_code,"
        + " uf_curr_name(A.curr_code) as tt_curr_code,"
        + " decode(A.apr_date,'','N','Y') as db_apr_flag, "
        + " (select usr_cname from sec_user where usr_id = A.crt_user) as tt_crt_user ";
    wp.daoTable = "cms_acaj A";
    wp.whereOrder = " order by 1 Asc , 2 Asc ";

    pageQuery(sqlParm.getConvParm());
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(1);
    wp.setPageValue();
  }

  void sum(String lsWhere) {
	  sqlParm.setSqlParmNoClear(true);  
    String sql1 = "select " + " sum(adj_amt) as tl_adj_amt_901 ," + " count(*) as db_cnt_901 "
        + " from cms_acaj " + lsWhere + " and curr_code = '901' ";
    sqlSelect(sql1);
    wp.colSet("tl_db_d_amt_901", "" + sqlNum("tl_adj_amt_901"));
    wp.colSet("tl_cnt_901", "" + sqlNum("db_cnt_901"));
    sqlParm.setSqlParmNoClear(true);
    String sql2 = "select " + " sum(adj_amt) as tl_adj_amt_840 ," + " count(*) as db_cnt_840 "
        + " from cms_acaj " + lsWhere + " and curr_code = '840' ";
    sqlSelect(sql2);
    wp.colSet("tl_db_d_amt_840", "" + sqlNum("tl_adj_amt_840"));
    wp.colSet("tl_cnt_840", "" + sqlNum("db_cnt_840"));
    sqlParm.setSqlParmNoClear(true);
    String sql3 = "select " + " sum(adj_amt) as tl_adj_amt_392 ," + " count(*) as db_cnt_392 "
        + " from cms_acaj " + lsWhere + " and curr_code = '392' ";
    sqlSelect(sql3);
    wp.colSet("tl_db_d_amt_392", "" + sqlNum("tl_adj_amt_392"));
    wp.colSet("tl_cnt_392", "" + sqlNum("db_cnt_392"));
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    int llOk = 0, llErr = 0, llErrApr = 0;
    cmsm02.Cmsp4030Func func = new cmsm02.Cmsp4030Func();
    func.setConn(wp);

    String[] lsRowid = wp.itemBuff("rowid");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = lsRowid.length;
    this.optNumKeep(lsRowid.length, aaOpt);
    String optt = aaOpt[0];
    int a = aaOpt.length;
    if(optt.isEmpty() || a==0) {
    	alertErr2("資料錯誤: 請點選欲覆核資料");
    	return;
    }

    for (int ii = 0; ii < aaOpt.length; ii++) {
      int rr = this.optToIndex(aaOpt[ii]);
      if (rr < 0)
        break;
      // -mod_user<>apr_user-
      if (wp.itemEq(rr, "crt_user", wp.loginUser)) {
        llErrApr++;
        continue;
      }

      String lsBatchNo = wp.itemStr(rr, "batch_no");
      // wp.ddd("%s. rowid[%s], batch_no[%s]",rr,ls_rowid[rr], ls_batch_no);

      wp.colSet(rr, "ok_flag", "!");
      func.varsSet("rowid", lsRowid[rr]);
      func.varsSet("batch_no", lsBatchNo);
      int liRc = func.dataProc();
      sqlCommit(liRc);
      optOkflag(rr, liRc);
      if (liRc == 1) {
        llOk++;
        wp.colSet(rr, "opt_on", "");
      } else if (liRc == -1) {
        llErr++;
      }
    }
    alertMsg("資料[覆核]處理完成; OK=" + llOk + ", ERR=" + llErr + ", 不可覆核=" + llErrApr);
  }

  void cancelFunc() {
    int llOK = 0;
    int llErr = 0;
    cmsm02.Cmsp4030Func func = new cmsm02.Cmsp4030Func();
    func.setConn(wp);

    String[] lsRowid = wp.itemBuff("rowid");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = lsRowid.length;
    this.optNumKeep(lsRowid.length, aaOpt);

    for (int ii = 0; ii < aaOpt.length; ii++) {
      int rr = this.optToIndex(aaOpt[ii]);
      if (rr < 0)
        break;

      String lsBatchNo = wp.itemStr(rr, "batch_no");

      wp.colSet(rr, "ok_flag", "!");
      func.varsSet("rowid", lsRowid[rr]);
      func.varsSet("batch_no", lsBatchNo);
      int liRc = func.cancelProc();
      sqlCommit(liRc);
      optOkflag(rr, liRc);
      if (liRc == 1) {
        llOK++;
        wp.colSet(rr, "opt_on", "");
      } else if (liRc == -1) {
        llErr++;
      }
    }
    alertMsg("資料[取消覆核]處理完成; OK=" + llOK + ", ERR=" + llErr);
  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    wp.colSet("ex_act_item1", "1");
    wp.colSet("ex_act_item2", "1");
    wp.colSet("ex_act_item3", "1");
   // wp.colSet("ex_adj_dept", "CS");
    wp.colSet("default_chk", "checked");
  }

}
