package cmsr02;
/** 
 * 2020-0717   JustinWu ++ default_chk in initPage
 * 20-0103:   Alex  dddw order by 
 * 19-1220:   Ru    失敗原因代碼轉中文
 * 19-0617:   JH    p_xxx >>acno_p_xxx
 *109-04-28  shiyuqi       updated for project coding standard     * 
 *109-07-15  Sunny  fix queryRead() 對應不到失敗原因代碼時顯示原值      
 *112-01-18  Machao     頁面bug調整          *                        *
 **/
import ofcapp.BaseAction;

public class Cmsr4070 extends BaseAction {

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
    } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
      procFunc();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    }

  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "cmsr4070")) {
        wp.optionKey = wp.colStr(0, "ex_curr_code");
        dddwList("dddw_dc_curr_code_tw", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type = 'DC_CURRENCY' order by 1 Desc ");
      }
    } catch (Exception ex) {
    }

  }

  @Override
  public void queryFunc() throws Exception {
    if (empty(wp.itemStr("ex_crt_date1")) && empty(wp.itemStr("ex_crt_date2"))
        && empty(wp.itemStr("ex_appr_no"))) {
      alertErr2("請輸入查詢條件");
      return;
    }


    if (this.chkStrend(wp.itemStr("ex_crt_date1"), wp.itemStr("ex_crt_date2")) == false) {
      alertErr2("登錄日期起迄：輸入錯誤");
      return;
    }

    String lsWhere = " where 1=1 " 
    	+ sqlCol(wp.itemStr("ex_crt_date1"), "A.crt_date", ">=")
        + sqlCol(wp.itemStr("ex_crt_date2"), "A.crt_date", "<=")
        + sqlCol(wp.itemStr("ex_appr_no"), "A.appr_no", "like%");

    if (wp.itemEq("ex_debit_flag", "Y")) {
      lsWhere += " and A.debit_flag ='Y'";
    } else if (wp.itemEq("ex_debit_flag", "N")) {
      lsWhere += " and A.debit_flag<>'Y'";
    }

    if (wp.itemEq("ex_err_code", "1")) {
      lsWhere += " and A.acct_errcode =''";
    } else if (wp.itemEq("ex_err_code", "2")) {
      lsWhere += " and A.acct_errcode <>''";
    }
    if (!empty(wp.itemStr("ex_curr_code"))) {
      lsWhere += sqlCol(wp.itemStr2("ex_curr_code"), "uf_nvl(A.curr_code,'901')");
    }
    lsWhere += " and A.acct_post_flag ='Y'";

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
        
    wp.selectSQL = " " + " A.appr_no ," + " uf_nvl(A.curr_code,'901') as curr_code ,"
        + " A.card_no ," + " A.crt_user ," + " A.acct_type ,"
        + " uf_acno_key2(A.p_seqno,A.debit_flag) as acct_key ," + " A.acct_code ,"
        + " A.reference_no ," + " A.adj_amt ," + " A.adj_remark ,"
        + " uf_card_name(A.card_no) as db_cname ," + " A.p_seqno ," + " A.crt_date ,"
        + " A.acct_errcode ," + " A.acct_no ," + " A.debit_flag ,"
        + " uf_nvl(A.curr_code,'901') as curr_code ,"
        + " uf_dc_amt(A.curr_code,A.d_avail_bal,A.dc_d_avail_bal) as d_available_bal ,"
        + " A.d_avail_bal as tw_d_available_bal ," + " A.debit_flag , "
        + " (select chi_short_name from ptr_actcode where acct_code =A.acct_code) as tt_acct_code , "
        + " (select usr_cname from sec_user where usr_id = A.crt_user ) as tt_crt_user , "
       // + " (select wf_desc from ptr_sys_idtab where wf_type='CMSR4070'and wf_id = A.acct_errcode ) as acct_errdesc ";
        +"decode(wf_desc,NULL,A.acct_errcode,wf_desc) as acct_errdesc";
    wp.daoTable = "cms_acaj A Left join (SELECT wf_desc,wf_id FROM ptr_sys_idtab  WHERE wf_type='CMSR4070')  B on B.wf_id = A.acct_errcode";
    wp.whereOrder = " order by 1 Asc, 2 Asc, 3 Asc ";
    logSql();
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
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_acct_key", wp.colStr(ii, "acct_type") + "_" + wp.colStr(ii, "acct_key"));
    }
  }

  void sum(String lsWhere) {
    String sql1 = "select " + " sum(adj_amt) as tl_adj_amt_901 ," + " count(*) as db_cnt_901 "
        + " from cms_acaj A " + lsWhere + " and curr_code = '901' ";
    sqlParm.setSqlParmNoClear(true);
    sqlSelect(sql1);
    wp.colSet("tl_db_d_amt_901", "" + sqlNum("tl_adj_amt_901"));
    wp.colSet("tl_cnt_901", "" + sqlNum("db_cnt_901"));

    String sql2 = "select " + " sum(adj_amt) as tl_adj_amt_840 ," + " count(*) as db_cnt_840 "
        + " from cms_acaj A" + lsWhere + " and curr_code = '840' ";
    sqlParm.setSqlParmNoClear(true);
    sqlSelect(sql2);
    wp.colSet("tl_db_d_amt_840", "" + sqlNum("tl_adj_amt_840"));
    wp.colSet("tl_cnt_840", "" + sqlNum("db_cnt_840"));

    String sql3 = "select " + " sum(adj_amt) as tl_adj_amt_392 ," + " count(*) as db_cnt_392 "
        + " from cms_acaj A" + lsWhere + " and curr_code = '392' ";
    sqlParm.setSqlParmNoClear(true);
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
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub
	  wp.colSet("default_chk", "checked");
  }

}
